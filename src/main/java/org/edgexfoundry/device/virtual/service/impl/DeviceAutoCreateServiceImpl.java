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
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.edgexfoundry.controller.AddressableClient;
import org.edgexfoundry.controller.DeviceClient;
import org.edgexfoundry.device.virtual.config.DeviceServiceProperties;
import org.edgexfoundry.device.virtual.data.DeviceStore;
import org.edgexfoundry.device.virtual.service.DeviceAutoCreateService;
import org.edgexfoundry.device.virtual.service.ProvisionService;
import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.AdminState;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.DeviceProfile;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.OperatingState;
import org.edgexfoundry.domain.meta.Protocol;

@Service
public class DeviceAutoCreateServiceImpl implements DeviceAutoCreateService {

	// private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// replace above logger with EdgeXLogger below
	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = org.edgexfoundry.support.logging.client.EdgeXLoggerFactory
			.getEdgeXLogger(this.getClass());

	@Autowired
	private DeviceServiceProperties deviceServiceProps;

	@Autowired
	private DeviceClient deviceClient;

	@Autowired
	private AddressableClient addressableClient;
	
	@Autowired
	private ProvisionService provisionService;

	@Autowired
	@Lazy
	private DeviceStore deviceStore;

	private boolean isCompleted = false;

	@Override
	public void autoCreateOneDeviceForEachProfile() {

		DeviceService deviceService = deviceServiceProps.getDeviceService();
		Collection<Device> existingDevices = deviceStore.getDevices().values();
		Set<DeviceProfile> profiles = deviceStore.getDevices().values().stream()
				.map(Device::getProfile).collect(Collectors.toSet());
		profiles.addAll(provisionService.getProvisionedProfiles());

		for (DeviceProfile deviceProfile : profiles) {
			if (isProfileWithoutDevice(existingDevices, deviceProfile)) {
				Device device = GenerateVirtualDevice(deviceService, deviceProfile);
				device.setOrigin(System.currentTimeMillis());
				deviceClient.add(device);
			}
		}

		isCompleted = true;

	}

	private Device GenerateVirtualDevice(DeviceService deviceService, DeviceProfile deviceProfile) {
		Device device = new Device();
		device.setService(deviceService);
		device.setProfile(deviceProfile);
		device.setName(deviceProfile.getName() + "01");
		device.setAdminState(AdminState.UNLOCKED);
		device.setOperatingState(OperatingState.ENABLED);
		device.setDescription("Auto-generate this virtual device. " + deviceProfile.getDescription());
		device.setLabels(deviceProfile.getLabels());
		createAssociatedAddressable(device);

		logger.debug("a default device created: " + device.toString());

		return device;
	}

	private void createAssociatedAddressable(Device device) {
		String addressableName = device.getName() + "-virtual-addressable";

		Addressable existingAddressable = fetchAddressable(addressableName);
		if (existingAddressable == null) {
			Addressable addressable = new Addressable(addressableName, Protocol.OTHER, deviceServiceProps.getHost(), "",
					deviceServiceProps.getPort());
			addressable.setOrigin(System.currentTimeMillis());
			String addressableId = addressableClient.add(addressable);
			addressable.setId(addressableId);
			device.setAddressable(addressable);
		} else {
			device.setAddressable(existingAddressable);
		}
	}

	private boolean isProfileWithoutDevice(Collection<Device> devices, DeviceProfile deviceProfile) {
		boolean result = true;

		for (Device device : devices) {
			if (device.getProfile().getId().equals(deviceProfile.getId())) {
				result = false;

				logger.debug("the DeviceProfile: " + deviceProfile.getName() + "has already had an associated Device: "
						+ device.getName());

				break;
			}
		}

		return result;
	}

	@Override
	public boolean isCompleted() {
		return isCompleted;
	}

	private Addressable fetchAddressable(String addressableName) {
		try {
			return addressableClient.addressableForName(addressableName);
		} catch (NotFoundException nfE) {
			return null;
		}
	}

}
