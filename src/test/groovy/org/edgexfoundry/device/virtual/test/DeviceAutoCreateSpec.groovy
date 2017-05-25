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

import java.util.Collection

import org.junit.experimental.categories.Category
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest

import org.edgexfoundry.controller.DeviceClient
import org.edgexfoundry.device.virtual.Application
import org.edgexfoundry.device.virtual.config.ApplicationProperties
import org.edgexfoundry.device.virtual.data.DeviceStore
import org.edgexfoundry.device.virtual.service.ApplicationInitializer
import org.edgexfoundry.domain.meta.Device
import org.edgexfoundry.domain.meta.DeviceProfile
import org.edgexfoundry.test.category.RequiresCoreDataRunning
import org.edgexfoundry.test.category.RequiresMetaDataRunning
import org.edgexfoundry.test.category.RequiresMongoDB
import org.edgexfoundry.test.category.RequiresSpring

import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Title

@Category([ RequiresSpring.class, RequiresMongoDB.class, RequiresMetaDataRunning.class, RequiresCoreDataRunning.class ])
@Narrative(""" When the application.auto-create-device configuration property set to true,
the application will create one Device instance for each managed Device Profile if there is no existing
Device instance for this Device Service.
""")
@Title("Check the Device Profile provision act as expected")
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
class DeviceAutoCreateSpec extends Specification {

	@Autowired
	ApplicationProperties applicationProps

	@Autowired
	DeviceClient deviceClient

	@Autowired
	ApplicationInitializer appInit

	@Autowired
	DeviceStore deviceStore

	def setupSpec(){
		println "Requires Core Data and Meta Data (both services and Mongo db) to be running first"
	}

	@Timeout(60)
	def "Check each managed Device Profile has at least one Device instance belonging to it"(){
		given: "application.auto-create-device=true"
		if(!applicationProps.isAutoCreateDevice()){
			println "Device auto-creations is not enabled, so skip this test"
			return
		}

		and: "the application is initialized"
		while (!appInit.isInit()) {
			sleep(500)
		}

		and: "get all the managed Device Profiles"
		Collection<Device> devices = deviceStore.getDevices().values()
		List<DeviceProfile> profileList = devices.stream().map{ it.profile }.distinct().collect()

		when: "query Devices by Device Profile"
		List<Device> deviceList = profileList.stream().flatMap{ deviceClient.devicesForProfile(it.id).stream() }.collect()

		then: "each Device Profile should get at least one Device instance"
		profileList.every{ profile ->
			deviceList.any { device ->
				device.profile.name == profile.name
			}
		}
	}
}
