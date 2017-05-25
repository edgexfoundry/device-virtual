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

import org.edgexfoundry.controller.EventClient
import org.edgexfoundry.device.virtual.Application
import org.edgexfoundry.device.virtual.config.ApplicationProperties
import org.edgexfoundry.device.virtual.data.DeviceStore
import org.edgexfoundry.device.virtual.service.ApplicationInitializer
import org.edgexfoundry.domain.core.Event
import org.edgexfoundry.domain.meta.Device
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
Device instance for this Device Service.  Then, CollectionTask should send Events to Core Data according to 
application.collection-frequency.
""")
@Title("The CollectionTask should send Events to Core Data periodically")
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
class CollectionTaskSpec extends Specification {
	
	static final int RESULT_LIMIT = 10

	@Autowired
	ApplicationProperties applicationProps

	@Autowired
	EventClient eventClient
	
	@Autowired
	ApplicationInitializer appInit
	
	@Autowired
	DeviceStore deviceStore
	
	def setupSpec(){
		println "Requires Core Data and Meta Data (both services and Mongo db) to be running first"
	}

	@Timeout(60)
	def "Check each managed Device has at least one Event belonging to it"(){
		given: "application.auto-create-device=true"
		if(!applicationProps.isAutoCreateDevice()){
			println "Device auto-creations is not enabled, so skip this test"
			return
		}

		and: "the application is initialized"
		while (!appInit.isInit()) {
			sleep(500)
		}

		and: "get all the managed Device"
		Collection<Device> devices = deviceStore.getDevices().values()
		
		and: "wait for the first batch of CollectionTasks"
		sleep(2 * applicationProps.collectionFrequency * 1000)
		
		when: "query Events from Core Data by Device"
		List<List<Event>> eventLists = devices.stream().map{ eventClient.eventsForDevice(it.name, RESULT_LIMIT) }.collect()

		then: "each Device should have at least 1 Event"
		eventLists.every{
			it.size() > 0
		}
	}
}
