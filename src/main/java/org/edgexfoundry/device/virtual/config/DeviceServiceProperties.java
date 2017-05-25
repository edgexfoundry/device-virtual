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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.edgexfoundry.domain.meta.Addressable;
import org.edgexfoundry.domain.meta.AdminState;
import org.edgexfoundry.domain.meta.DeviceService;
import org.edgexfoundry.domain.meta.OperatingState;
import org.edgexfoundry.domain.meta.Protocol;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@ConfigurationProperties("service")
public class DeviceServiceProperties {
	
	//private final Logger logger = LoggerFactory.getLogger(this.getClass());
	//replace above logger with EdgeXLogger below
	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = 
			org.edgexfoundry.support.logging.client.EdgeXLoggerFactory.getEdgeXLogger(this.getClass());

	private String name;
	private Protocol protocol;
	private String host;
	private int port;
	private String[] labels;
	private AdminState adminState;
	private OperatingState operatingState;
	private String callback;
	private DeviceService deviceService;
	
	@PostConstruct
	private void initDeviceService() {
		if (deviceService == null) {
			deviceService = new DeviceService();
			deviceService.setName(name);
			deviceService.setLabels(labels);
			deviceService.setAdminState(adminState);
			deviceService.setOperatingState(operatingState);

			Addressable addressable = new Addressable(name + "CallbackURL", protocol, host,
					callback, port);
			deviceService.setAddressable(addressable);
			
			logger.debug("set up initial DeviceServce: " + deviceService.toString());
		}
	}

	public DeviceService getDeviceService() {
		if (deviceService == null) {
			initDeviceService();
		}
		return deviceService;
	}

	public void setDeviceService(DeviceService deviceService) {
		this.deviceService = deviceService;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public AdminState getAdminState() {
		return adminState;
	}

	public void setAdminState(AdminState adminState) {
		this.adminState = adminState;
	}

	public OperatingState getOperatingState() {
		return operatingState;
	}

	public void setOperatingState(OperatingState operatingState) {
		this.operatingState = operatingState;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

}
