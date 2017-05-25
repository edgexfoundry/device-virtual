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

import javax.annotation.PostConstruct;
import javax.ws.rs.ProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import org.edgexfoundry.controller.PingCoreDataClient;
import org.edgexfoundry.controller.PingMetaDataClient;
import org.edgexfoundry.device.virtual.config.ApplicationProperties;
import org.edgexfoundry.device.virtual.config.DeviceServiceProperties;
import org.edgexfoundry.device.virtual.controller.StatusController;
import org.edgexfoundry.device.virtual.service.ApplicationInitializer;
import org.edgexfoundry.device.virtual.service.DeviceAutoCreateService;
import org.edgexfoundry.device.virtual.service.ProvisionService;
import org.edgexfoundry.device.virtual.service.VirtualResourceManager;
import org.edgexfoundry.exception.controller.ServiceException;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ApplicationInitializerImpl implements ApplicationInitializer {

	//private final Logger logger = LoggerFactory.getLogger(this.getClass());
	//replace above logger with EdgeXLogger below
	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = 
			org.edgexfoundry.support.logging.client.EdgeXLoggerFactory.getEdgeXLogger(this.getClass());

	private String selfPingURL;
	
	private boolean isInit = false;

	@Autowired
	private ProvisionService deviceProvisionService;

	@Autowired
	private DeviceAutoCreateService deviceAutoCreateService;

	@Autowired
	private VirtualResourceManager virtualResourceManager;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private DeviceServiceProperties deviceServiceProps;

	@Autowired
	private PingMetaDataClient pingMetaDataClient;

	@Autowired
	private PingCoreDataClient pingCoreDataClient;

	@PostConstruct
	private void postConstruct() {
		selfPingURL = "http://localhost:" + deviceServiceProps.getPort() + "/api/v1/ping";
	}

	@Override
	@Async
	public void init() {
		confirmDependenciesStarted();

		deviceProvisionService.doProvision();

		virtualResourceManager.createDefaultRecordsForExistingDevices();

		if (applicationProperties.isAutoCreateDevice()) {
			deviceAutoCreateService.autoCreateOneDeviceForEachProfile();
		}
		
		this.isInit = true;
	}

	private void confirmDependenciesStarted() {
		confirmLocalWebAppStarted();
		confirmMetaDataStarted();
		confirmCoreDataStarted();
	}

	private void confirmLocalWebAppStarted() {
		try {
			int connectFailedCount = 0;
			while (true) {
				Thread.sleep(3000L);
				RestTemplate restTemplate = new RestTemplate();
				String pingResponse;
				try {
					pingResponse = restTemplate.getForObject(this.selfPingURL, String.class);
				} catch (ResourceAccessException e) {
					if (connectFailedCount > 60) {
						throw e;
					}
					Thread.sleep(1000L);
					connectFailedCount++;
					continue;
				}
				if (StatusController.PING_RESPONSE.equals(pingResponse)) {
					break;
				} else {
					String errorMsg = "The response of ping request is not " + StatusController.PING_RESPONSE;
					logger.error(errorMsg);
					throw new RuntimeException(errorMsg);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException(e);
		}
	}

	private void confirmMetaDataStarted() {
		try {
			int connectFailedCount = 0;
			while (true) {
				String pingResponse;
				try {
					pingResponse = pingMetaDataClient.ping();
				} catch (ProcessingException | ResourceAccessException e) {
					if (connectFailedCount > 200) {
						throw e;
					}
					logger.info("waiting for Core Meta Data Micro Service fully starts up...");
					Thread.sleep(3000L);
					connectFailedCount++;
					continue;
				}
				if (StatusController.PING_RESPONSE.equals(pingResponse)) {
					break;
				} else {
					String errorMsg = "The response of ping request is not " + StatusController.PING_RESPONSE;
					logger.error(errorMsg);
					throw new RuntimeException(errorMsg);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException(e);
		}
	}

	private void confirmCoreDataStarted() {
		try {
			int connectFailedCount = 0;
			while (true) {
				String pingResponse;
				try {
					pingResponse = pingCoreDataClient.ping();
				} catch (ProcessingException | ResourceAccessException e) {
					if (connectFailedCount > 200) {
						throw e;
					}
					logger.info("waiting for Core Data Micro Service fully starts up...");
					Thread.sleep(3000L);
					connectFailedCount++;
					continue;
				}
				if (StatusController.PING_RESPONSE.equals(pingResponse)) {
					break;
				} else {
					String errorMsg = "The response of ping request is not " + StatusController.PING_RESPONSE;
					logger.error(errorMsg);
					throw new RuntimeException(errorMsg);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException(e);
		}
	}

	@Override
	public boolean isInit() {
		return isInit;
	}

}
