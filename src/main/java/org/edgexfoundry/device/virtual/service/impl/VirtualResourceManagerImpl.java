/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice:  device-virtual
 * @author: Cloud Tsai, Dell
 * @version: 1.0.0
 *******************************************************************************/
package org.edgexfoundry.device.virtual.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.ScheduleClient;
import org.edgexfoundry.controller.ScheduleEventClient;
import org.edgexfoundry.controller.ValueDescriptorClient;
import org.edgexfoundry.device.virtual.config.ApplicationProperties;
import org.edgexfoundry.device.virtual.config.DeviceServiceProperties;
import org.edgexfoundry.device.virtual.data.DeviceStore;
import org.edgexfoundry.device.virtual.data.VirtualResourceRepository;
import org.edgexfoundry.device.virtual.domain.VirtualResource;
import org.edgexfoundry.device.virtual.service.ReadValueGenerator;
import org.edgexfoundry.device.virtual.service.VirtualResourceManager;
import org.edgexfoundry.domain.common.HTTPMethod;
import org.edgexfoundry.domain.common.ValueDescriptor;
import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.Command;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceProfile;
import org.edgexfoundry.domain.meta.Get;
import org.edgexfoundry.domain.meta.Response;
import org.edgexfoundry.domain.meta.Schedule;
import org.edgexfoundry.domain.meta.ScheduleEvent;
import org.edgexfoundry.exception.controller.DataValidationException;

@Service
public class VirtualResourceManagerImpl implements VirtualResourceManager {

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// replace above logger with EdgeXLogger below
	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = org.edgexfoundry.support.logging.client.EdgeXLoggerFactory
			.getEdgeXLogger(this.getClass());

	private static final String POSITIVE_CODE = "200";

	@Autowired
	private VirtualResourceRepository virtualResourceRepository;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ReadValueGenerator readValueGenerator;

	// Client to fetch schedule events
	@Autowired
	private ScheduleEventClient scheduleEventClient;

	// Client to fetch schedules
	@Autowired
	private ScheduleClient scheduleClient;

	@Autowired
	private AddressableClient addressableClient;

	@Autowired
	private ValueDescriptorClient valueDescriptorClient;

	@Autowired
	private DeviceServiceProperties deviceServiceProperties;
	
	@Autowired
	@Lazy
	private DeviceStore deviceStore;

	@Override
	public void createDefaultRecordsForExistingDevices() {
		Collection<Device> devices = deviceStore.getDevices().values();

		for (Device device : devices) {
			createDefaultRecords(device);
		}
	}

	@Override
	public void createDefaultRecords(Device device) {
		Map<String, VirtualResource> vrMap = new HashMap<>();
		DeviceProfile profile = device.getProfile();
		List<Command> cmdList = profile.getCommands();
		for (Command command : cmdList) {
			if (command.getGet() == null) {
				continue;
			}
			Response response = extractPositiveResponse(command.getGet());
			List<String> expectedValueNames = response.getExpectedValues();
			List<ValueDescriptor> valueDescList = expectedValueNames.stream()
					.map(valueDescriptorClient::valueDescriptorByName).collect(Collectors.toList());

			if (valueDescList.stream().anyMatch(vd -> vd == null)) {
				logger.error("Some ValueDescriptors are not defined");
				valueDescList = valueDescList.stream().filter(vd -> vd != null).collect(Collectors.toList());
			}

			for (ValueDescriptor valueDesc : valueDescList) {
				if (vrMap.containsKey(valueDesc.getName()))
					continue;
				VirtualResource vr = new VirtualResource();
				vr.setDeviceId(device.getId());
				vr.setDeviceName(device.getName());
				vr.setProfileId(profile.getId());
				vr.setProfileName(profile.getName());
				vr.setCommandId(command.getId());
				vr.setCommandName(command.getName());
				vr.setCollectionFrequency(applicationProperties.getCollectionFrequency());
				vr.setResource(valueDesc.getName());
				vr.setType(valueDesc.getType());
				if (valueDesc.getMax() != null)
					vr.setMax(valueDesc.getMax().toString());
				if (valueDesc.getMin() != null)
					vr.setMin(valueDesc.getMin().toString());
				vr.setValue(readValueGenerator.generateValue(vr));

				vrMap.put(valueDesc.getName(), vr);
			}
			
			vrMap.entrySet().stream().parallel().forEach(entry -> {
				VirtualResource vr = entry.getValue();
				virtualResourceRepository.save(vr);
				createScheduler(vr);
				createSchedulerEvent(vr);
				logger.debug("created a virtual resource: " + vr.toString());
			});
		}
	}

	private Response extractPositiveResponse(Get getMethod) {
		Response result = null;
		List<Response> responses = getMethod.getResponses();

		if (responses == null) {
			String errMsg = "responses of Get are not defined: " + getMethod.toString();
			logger.error(errMsg);
			throw new DataValidationException(errMsg);
		} else {
			for (Response response : responses) {
				if (POSITIVE_CODE.equals(response.getCode())) {
					result = response;
					break;
				}
			}
		}
		return result;
	}

	private void createScheduler(VirtualResource vr) {
		String name = "interval-for-vr-" + String.valueOf(vr.getResourceId());
		String start = null;
		String end = null;
		String frequency = "PT" + String.valueOf(vr.getCollectionFrequency()) + "S";
		String cron = null;
		boolean runOnce = false;

		Schedule schedule = new Schedule(name, start, end, frequency, cron, runOnce);
		schedule.setOrigin(System.currentTimeMillis());

		try {
			scheduleClient.add(schedule);
		} catch (ClientErrorException cee) {
			logger.info("the schedule exists in metadata, so update it: " + schedule.toString());
			scheduleClient.update(schedule);
		} catch (Exception e) {
			logger.error("schedule for VirtualResource: " + vr.getResourceId() + " created failed", e);
		}
	}

	private void createSchedulerEvent(VirtualResource vr) {
		Addressable serviceAddressable = deviceServiceProperties.getDeviceService().getAddressable();
		String name = "device-virtual-vr-" + String.valueOf(vr.getResourceId());
		String schedule = "interval-for-vr-" + String.valueOf(vr.getResourceId());
		String parameters = null;
		String service = deviceServiceProperties.getDeviceService().getName();
		String path = "/api/v1/collector/" + String.valueOf(vr.getResourceId());

		Addressable addressable = new Addressable("Schedule-" + name, serviceAddressable.getProtocol(),
				serviceAddressable.getAddress(), path, serviceAddressable.getPort());
		addressable.setMethod(HTTPMethod.POST);
		addressable.setOrigin(System.currentTimeMillis());

		try {
			addressable.setId(addressableClient.add(addressable));
		} catch (ClientErrorException cee) {
			logger.info("the schedule event addressable exists in metadata, so update it: " + addressable.toString());
			addressableClient.update(addressable);
		} catch (Exception e) {
			logger.error("schedule event addressable for VirtualResource: " + vr.getResourceId() + " created failed", e);
		}

		ScheduleEvent scheduleEvent = new ScheduleEvent(name, addressable, parameters, schedule, service);
		scheduleEvent.setOrigin(System.currentTimeMillis());

		try {
			scheduleEventClient.add(scheduleEvent);
		} catch (ClientErrorException cee) {
			logger.info("the schedule event exists in metadata, so update it: " + scheduleEvent.toString());
			scheduleEventClient.update(scheduleEvent);
		} catch (Exception e) {
			logger.error("schedule event for VirtualResource: " + vr.getResourceId() + " created failed", e);
		}
	}

	@Override
	public VirtualResource findById(long resourceId) {
		return virtualResourceRepository.findOne(resourceId);
	}

	@Override
	public List<VirtualResource> findByDeviceIdAndCommandName(String deviceId, String commandName) {
		return virtualResourceRepository.findByDeviceIdAndCommandName(deviceId, commandName);
	}

	@Override
	public Long deleteByDevice(String deviceId) {
		return virtualResourceRepository.deleteByDeviceId(deviceId);
	}

	@Override
	public void regenerateValue(VirtualResource vr) {
		if (vr != null && vr.isEnableRandomization()) {
			vr.setValue(readValueGenerator.generateValue(vr));
			virtualResourceRepository.save(vr);
		}
	}

	@Override
	public void save(VirtualResource vr) {
		virtualResourceRepository.save(vr);
	}

	@Override
	public VirtualResource findByDeviceIdAndResource(String deviceId, String resource) {
		return virtualResourceRepository.findByDeviceIdAndResource(deviceId, resource);
	}

}
