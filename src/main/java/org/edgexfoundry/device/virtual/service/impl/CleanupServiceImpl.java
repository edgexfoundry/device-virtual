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
import java.util.List;
import java.util.Objects;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.DeviceClient;
import org.edgexfoundry.controller.DeviceProfileClient;
import org.edgexfoundry.controller.DeviceServiceClient;
import org.edgexfoundry.controller.EventClient;
import org.edgexfoundry.controller.ScheduleClient;
import org.edgexfoundry.controller.ScheduleEventClient;
import org.edgexfoundry.controller.ValueDescriptorClient;
import org.edgexfoundry.device.virtual.config.DeviceServiceProperties;
import org.edgexfoundry.device.virtual.controller.UpdateController;
import org.edgexfoundry.device.virtual.data.DeviceStore;
import org.edgexfoundry.device.virtual.data.ProfileStore;
import org.edgexfoundry.device.virtual.scheduling.Scheduler;
import org.edgexfoundry.device.virtual.service.CleanupService;
import org.edgexfoundry.domain.common.ValueDescriptor;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.ScheduleEvent;

@Service
public class CleanupServiceImpl implements CleanupService {

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// replace above logger with EdgeXLogger below
	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = org.edgexfoundry.support.logging.client.EdgeXLoggerFactory
			.getEdgeXLogger(this.getClass());

	public static boolean IS_CLEANING = false;

	@Autowired
	private DeviceServiceProperties deviceServiceProps;

	@Autowired
	private DeviceServiceClient deviceServiceClient;

	@Autowired
	private DeviceProfileClient deviceProfileClient;

	@Autowired
	private DeviceClient deviceClient;

	@Autowired
	private AddressableClient addressableClient;

	@Autowired
	private ValueDescriptorClient valueDescriptorClient;

	@Autowired
	private EventClient eventClient;

	@Autowired
	private ScheduleEventClient scheduleEventClient;

	@Autowired
	private ScheduleClient scheduleClient;

	@Autowired
	private UpdateController updateController;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private DeviceStore deviceStore;

	@Autowired
	private ProfileStore profileStore;

	@Override
	public void doCleanup() {

		IS_CLEANING = true;

		try {
			scheduler.clear();

			removeDevices();

			removeDeviceProfiles();

			removeDeviceService();

			removeValueDescriptors();

			removeSchedulesAndEvents();
		} catch (Exception e) {
			logger.error("Something is going wrong when cleaning up the virtual device data", e);
		}

		logger.debug("UpdateController needs to live until cleanup completed: " + updateController.toString());
		logger.debug("Virtual Device Service cleanup is completed");

		IS_CLEANING = false;
	}

	private void removeDevices() {
		Collection<Device> devices = deviceStore.getDevices().values();

		devices.stream().parallel().forEach(device -> {
			eventClient.deleteByDevice(device.getName());
			deviceClient.delete(device.getId());
			addressableClient.delete(device.getAddressable().getId());
		});

		logger.debug("the Device, Event, and Reading records have been deleted");
	}

	private void removeDeviceProfiles() {
		Collection<Device> devices = deviceStore.getDevices().values();

		devices.stream().map(d -> d.getProfile().getId()).distinct().forEach(id -> {
			try {
				deviceProfileClient.delete(id);
			} catch (ClientErrorException e) {
				if (e.getResponse().getStatus() == Status.CONFLICT.getStatusCode()) {
					// ignore the error because there might be other device
					// associated to this profile
					logger.info("delete device profile (" + id
							+ ") failed, because there might be other device associated to this profile "
							+ e.getMessage());
				} else {
					throw e;
				}
			}
		});

		logger.debug("the DeviceProfile records have been deleted");
	}

	private void removeDeviceService() {
		deviceServiceClient.delete(deviceServiceProps.getDeviceService().getId());
		addressableClient.delete(deviceServiceProps.getDeviceService().getAddressable().getId());

		logger.debug("the DeviceService record has been deleted");
	}

	private void removeValueDescriptors() {
		List<ValueDescriptor> valueDescriptorList = profileStore.getValueDescriptors();
		valueDescriptorList.stream().map(ValueDescriptor::getId).filter(Objects::nonNull).distinct()
				.forEach(valueDescriptorClient::delete);

		logger.debug("the ValueDescriptor records have been deleted");

	}

	private void removeSchedulesAndEvents() {
		List<ScheduleEvent> scheduleEventList = scheduleEventClient
				.scheduleEventsForServiceByName(deviceServiceProps.getName());
		scheduleEventList.stream().forEach(se -> {
			scheduleEventClient.delete(se.getId());
			addressableClient.delete(se.getAddressable().getId());
			try {
				scheduleClient.deleteByName(se.getSchedule());
			} catch (ClientErrorException e) {
				if (e.getResponse().getStatus() == Status.CONFLICT.getStatusCode()) {
					// ignore the error because there might be other schedule
					// event associated to this schedule
					logger.info("delete schedule (" + se.getSchedule()
							+ ") failed, because there might be other schedule event associated to this schedule "
							+ e.getMessage());
				} else {
					throw e;
				}
			}
		});

		logger.debug("the Schedule And ScheduleEvent records have been deleted");
	}

}
