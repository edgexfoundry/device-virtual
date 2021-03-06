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
package org.edgexfoundry.device.virtual.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("application")
public class ApplicationProperties {
	
	private List<String> deviceProfilePaths;
	
	private boolean autoCleanup;

	private boolean autoCreateDevice;
	
	private int collectionFrequency;
	
	public List<String> getDeviceProfilePaths() {
		return deviceProfilePaths;
	}

	public void setDeviceProfilePaths(List<String> deviceProfilePaths) {
		this.deviceProfilePaths = deviceProfilePaths;
	}

	public boolean isAutoCleanup() {
		return autoCleanup;
	}

	public void setAutoCleanup(boolean autoCleanup) {
		this.autoCleanup = autoCleanup;
	}

	public boolean isAutoCreateDevice() {
		return autoCreateDevice;
	}

	public void setAutoCreateDevice(boolean autoCreateDevice) {
		this.autoCreateDevice = autoCreateDevice;
	}

	public int getCollectionFrequency() {
		return collectionFrequency;
	}

	public void setCollectionFrequency(int collectionFrequency) {
		this.collectionFrequency = collectionFrequency;
	}
	
}
