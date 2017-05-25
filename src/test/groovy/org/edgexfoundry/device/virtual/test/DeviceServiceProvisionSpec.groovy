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
package org.edgexfoundry.device.virtual.test

import org.junit.experimental.categories.Category
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest

import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import org.edgexfoundry.device.virtual.Application
import org.edgexfoundry.controller.DeviceServiceClient
import org.edgexfoundry.device.virtual.config.DeviceServiceProperties
import org.edgexfoundry.device.virtual.service.ApplicationInitializer
import org.edgexfoundry.domain.meta.Addressable
import org.edgexfoundry.domain.meta.DeviceService
import org.edgexfoundry.test.category.RequiresCoreDataRunning
import org.edgexfoundry.test.category.RequiresMetaDataRunning
import org.edgexfoundry.test.category.RequiresMongoDB
import org.edgexfoundry.test.category.RequiresSpring

@Category([ RequiresSpring.class, RequiresMongoDB.class, RequiresMetaDataRunning.class, RequiresCoreDataRunning.class ])
@Narrative(""" When the Virtual Device Service starts up, the application
will read a configuration file to get the Device Service information.
The default configuration file is application.properties
However, users can override the configuration by:
1. Command-line arguments 
2. JNDI attributes from java:comp/env
3. JVM system properties
4. Operating system environment variables
5. An application.properties or application.yml file outside of the application
(in the directory or a /config subdirectory of the directory from which the application is run)
 
The name property of device service should be provided, and the same Device Service won't be 
registered repeatedly whenever the application restart. 
Then, it will invoke Meta Data Service to add an instance of the Device Service if it
is not existing.
""")
@Title("Check the Device Service provision act as expected")
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
class DeviceServiceProvisionSpec extends Specification {
	
	@Autowired
	DeviceServiceClient deviceServiceClient
	
	@Autowired
	DeviceServiceProperties deviceServiceProps
	
	@Autowired
	ApplicationInitializer appInit
	
	def setupSpec(){
		println "Requires Core Data and Meta Data (both services and Mongo db) to be running first"
	}
	
	def "Check the Device Service instance can be retrieved from MetaData Service"() {
		given: "the application is initialized"
		while (!appInit.isInit()) {
			sleep(500)
		}
		
		and: "create a MetaData REST client of DeviceService"
		//deviceServiceClient is injected by Spring automatically

		and: "read the DeviceService properties from src/test/resources/application.properties"
		//deviceServiceProps read application.properties and is injected by Spring automatically

		when: "call MetaData API to retrieve DeviceService by name"
		DeviceService deviceService = deviceServiceClient.deviceServiceForName(deviceServiceProps.name)

		then: "the device service exists"
		deviceService
		
		and: "confirm the DeviceService exists in MetaData and all properties match"
		with(deviceService){
			name == deviceServiceProps.name
			Arrays.equals(labels, deviceServiceProps.labels)
			adminState == deviceServiceProps.adminState
			operatingState == deviceServiceProps.operatingState
		}
		
		Addressable addressable = deviceService.addressable
		with(addressable) {
			protocol == deviceServiceProps.protocol
			address == deviceServiceProps.host
			port == deviceServiceProps.port
			path == deviceServiceProps.callback
		}

	}

}
