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
package org.edgexfoundry.device.virtual.service;

import java.util.List;

import org.edgexfoundry.device.virtual.domain.VirtualResource;
import org.edgexfoundry.domain.meta.Device;

public interface VirtualResourceManager {
	
	public void createDefaultRecordsForExistingDevices();
	
	public void createDefaultRecords(Device device);
	
	public VirtualResource findById(long resourceId);
	
	public List<VirtualResource> findByDeviceIdAndCommandName (String deviceId, String commandName);
	
	public VirtualResource findByDeviceIdAndResource (String deviceId, String resource);
	
	public Long deleteByDevice(String deviceId);
	
	public void save(VirtualResource vr);
	
	public void regenerateValue(VirtualResource vr);

}
