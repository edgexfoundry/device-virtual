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
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest

import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import org.edgexfoundry.device.virtual.Application
import org.edgexfoundry.controller.DeviceProfileClient
import org.edgexfoundry.device.virtual.config.DeviceServiceProperties
import org.edgexfoundry.device.virtual.service.ApplicationInitializer
import org.edgexfoundry.device.virtual.service.YamlReader
import org.edgexfoundry.domain.meta.DeviceProfile
import org.edgexfoundry.test.category.RequiresCoreDataRunning
import org.edgexfoundry.test.category.RequiresMetaDataRunning
import org.edgexfoundry.test.category.RequiresMongoDB
import org.edgexfoundry.test.category.RequiresSpring

@Category([ RequiresSpring.class, RequiresMongoDB.class, RequiresMetaDataRunning.class, RequiresCoreDataRunning.class ])
@Narrative(""" When the Virtual Device Service starts up, the application
will read \${application.device-profile-paths} variable to get the folders which contains 
device profile definitions in YAML.
In this test, the folders are /src/test/resources/bacnet_test_profiles and /src/test/resources/modbus_test_profiles. 
They are defined in src/test/resources/application.properties
However, users can override the configuration by:
1. Command-line arguments 
2. JNDI attributes from java:comp/env
3. JVM system properties
4. Operating system environment variables
5. An application.properties or application.yml file outside of the application
(in the directory or a /config subdirectory of the directory from which the application is run)

In accordance with 'Device Micro Service(s) Specification Documents'-
'The device profile name should be unique within the scope of a Device service.'
As a result, if the DeviceProfile name exist in MetaData with the same DeviceService, it will 
be ignored and not be added again.
""")
@Title("Check the Device Profile provision act as expected")
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
class DeviceProfileProvisionSpec extends Specification {

	@Autowired
	DeviceProfileClient deviceProfileClient

	@Autowired
	DeviceServiceProperties deviceServiceProps

	@Autowired
	YamlReader yamlReader

	@Value("\${application.device-profile-paths}")
	String[] profileYamlPaths

	@Autowired
	ApplicationInitializer appInit

	def setupSpec(){
		println "Requires Core Data and Meta Data (both services and Mongo db) to be running first"
	}

	def "Check the Device Profile instances can be retrieved from MetaData Service"() {
		given: "the application is initialized"
		while (!appInit.isInit()) {
			sleep(500)
		}

		and: "create a MetaData REST client of DeviceProfile"
		//deviceProfileClient is injected by Spring automatically

		and: "read the DeviceProfile properties from $profileYamlPaths"
		List<Map> yamlList = readYamlFromPaths()

		when: "call MetaData API to retrieve DeviceProfiles by service"
		List<DeviceProfile> profileList = yamlList.stream().map{
			deviceProfileClient.deviceProfileForName(it["name"])}.collect()

		then: "confirm the DeviceProfiles retrieved from MetaData match the ones from YAML"
		profileList.size() == yamlList.size()
		profileList.every { profile ->
			yamlList.any { it["name"] == profile.name }
		}
	}

	private List<Map> readYamlFromPaths() {
		return Arrays.stream(profileYamlPaths).map{ 
			yamlReader.listYamlFiles(it) }.flatMap{ 
			Arrays.stream(it)}.map{  yamlReader.readYamlFileAsMap(it) }.collect()
	}

}
