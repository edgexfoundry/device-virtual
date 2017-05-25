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
package org.edgexfoundry.device.virtual.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.edgexfoundry.domain.common.IoTType;

@Entity
public class VirtualResource {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long resourceId;
	private String deviceId;
	private String deviceName;
	private String profileId;
	private String profileName;
	private String commandId;
	private String commandName;
	private String resource;
	private IoTType type;
	private String min;
	private String max;
	private String value;
	private boolean enableRandomization = true;
	private long collectionFrequency;

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public IoTType getType() {
		return type;
	}

	public void setType(IoTType type) {
		this.type = type;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEnableRandomization() {
		return enableRandomization;
	}

	public void setEnableRandomization(boolean enableRandomization) {
		this.enableRandomization = enableRandomization;
	}

	public long getCollectionFrequency() {
		return collectionFrequency;
	}

	public void setCollectionFrequency(long collectionFrequency) {
		this.collectionFrequency = collectionFrequency;
	}

	@Override
	public String toString() {
		return "VirtualResource [resourceId=" + resourceId + ", deviceId=" + deviceId + ", deviceName=" + deviceName
				+ ", profileId=" + profileId + ", profileName=" + profileName + ", commandId=" + commandId
				+ ", commandName=" + commandName + ", resource=" + resource + ", type=" + type + ", min=" + min
				+ ", max=" + max + ", value=" + value + ", enableRandomization=" + enableRandomization
				+ ", collectionFrequency=" + collectionFrequency + "]";
	}

}
