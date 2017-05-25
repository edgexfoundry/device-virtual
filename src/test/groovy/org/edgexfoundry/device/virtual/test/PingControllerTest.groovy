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

import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

import org.edgexfoundry.device.virtual.controller.StatusController
import org.edgexfoundry.test.category.RequiresNone

@Category([ RequiresNone.class ])
@Title("Check the Ping function acts as expected")
@Subject(StatusController.class)
class PingControllerTest extends Specification {
	
	static final String PING_RESP = "pong"
	 
	StatusController controller
	
	def setup() {
		controller = new StatusController()
	}
	
	def "The ping controller should return 'pong' as the response"() {
		expect:
		controller.ping() == PING_RESP
	}

}
