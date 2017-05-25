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
 * @microservice:  device-sdk-tools
 * @author: Tyler Cox, Dell
 * @version: 1.0.0
 *******************************************************************************/
package org.edgexfoundry.device.virtual;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.edgexfoundry.device.virtual.data.DeviceStore;
import org.edgexfoundry.device.virtual.data.ObjectStore;
import org.edgexfoundry.device.virtual.data.ProfileStore;
import org.edgexfoundry.device.virtual.domain.ScanList;
import org.edgexfoundry.device.virtual.domain.VirtualObject;
import org.edgexfoundry.device.virtual.domain.VirtualResource;
import org.edgexfoundry.device.virtual.handler.VirtualHandler;
import org.edgexfoundry.device.virtual.service.ApplicationInitializer;
import org.edgexfoundry.device.virtual.service.VirtualResourceManager;
import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.Device;
import org.edgexfoundry.domain.meta.ResourceOperation;
import org.edgexfoundry.exception.controller.ServiceException;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;

@Service
public class VirtualDriver {

	private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(VirtualDriver.class);

	@Autowired
	ProfileStore profiles;

	@Autowired
	@Lazy
	DeviceStore devices;

	@Autowired
	ObjectStore objectCache;

	@Autowired
	VirtualHandler handler;

	@Autowired
	ApplicationInitializer applicationInitializer;

	@Autowired
	private VirtualResourceManager virtualResourceManager;

	public enum supportedPutCommandParameter {
		enableRandomization, collectionFrequency
	}

	public ScanList discover() {
		ScanList scan = new ScanList();

		// discovery is not used

		return scan;
	}

	// operation is get or set
	// Device to be written to
	// Virtual Object to be written to
	// value is string to be written or null
	public void process(ResourceOperation operation, Device device, VirtualObject object, String value,
			String transactionId, String opId, List<ResourceOperation> operations) {

		// TODO 2: [Optional] Modify this processCommand call to pass any
		// additional required metadata from the profile to the driver stack
		String result = processCommand(operation.getOperation(), object, value, device, operations);

		objectCache.put(device, operation, result);
		handler.completeTransaction(transactionId, opId, objectCache.getResponses(device, operation));
	}

	// Modify this function as needed to pass necessary metadata from the device
	// and its profile to the driver interface
	public String processCommand(String operation, VirtualObject object, String value, Device device,
			List<ResourceOperation> getOperations) {

		logger.debug("process command: operation-" + operation + ", DeviceObject-" + object.toString() + ", on Device-"
				+ device.getName());
		String result;

		// from the device as a string for processing
		if (operation.toLowerCase().equals("get")) {
			VirtualResource vr = virtualResourceManager.findByDeviceIdAndResource(device.getId(), object.getName());
			result = vr.getValue();
		} else {
			processPutCommand(object, value, device, getOperations);
			result = value;
		}

		return result;
	}

	private void processPutCommand(VirtualObject object, String value, Device device,
			List<ResourceOperation> getOperations) {
		List<VirtualResource> vrList = getOperations.stream()
				.map(op -> virtualResourceManager.findByDeviceIdAndResource(device.getId(), op.getObject()))
				.collect(Collectors.toList());
		if (supportedPutCommandParameter.enableRandomization.toString().equals(object.getName())) {
			vrList.stream().forEach(vr -> vr.setEnableRandomization(Boolean.valueOf(value)));
		} else if (supportedPutCommandParameter.collectionFrequency.toString().equals(object.getName())) {
			vrList.stream().forEach(vr -> vr.setCollectionFrequency(Long.valueOf(value)));
		} else {
			throw new ServiceException(new UnsupportedOperationException(
					"Only enableRandomization and collectionFrequency could be the Put Command parameters"));
		}
		vrList.stream().forEach(virtualResourceManager::save);
	}

	public void initialize() {
		applicationInitializer.init();
	}

	public void disconnectDevice(Addressable address) {
		// TODO 6: [Optional] Disconnect devices here using driver level
		// operations

	}

	@SuppressWarnings("unused")
	private void receive() {
		// TODO 7: [Optional] Fill with your own implementation for handling
		// asynchronous data from the driver layer to the device service
		Device device = null;
		String result = "";
		ResourceOperation operation = null;

		objectCache.put(device, operation, result);
	}

}
