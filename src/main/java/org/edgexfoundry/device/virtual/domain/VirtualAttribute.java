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
 * @microservice:  device-sdk-tools
 * @author: Tyler Cox, Dell
 * @version: 1.0.0
 *******************************************************************************/
package org.edgexfoundry.device.virtual.domain;

import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;
import com.google.gson.Gson;

public class VirtualAttribute {
	
	private final static EdgeXLogger logger = EdgeXLoggerFactory.getEdgeXLogger(VirtualAttribute.class);
	
	// Replace these attributes with the Virtual
	// specific metadata needed by the Virtual Driver
	
	private String instance;
	private String type;
	
	public VirtualAttribute(Object attributes) {
		try {
			Gson gson = new Gson();
			String jsonString = gson.toJson(attributes);
			VirtualAttribute thisObject = gson.fromJson(jsonString, this.getClass());
			
			this.setInstance(thisObject.getInstance());
			this.setType(thisObject.getType());
			
		} catch (Exception e) {
			logger.error("Cannot Construct VirtualAttribute: " + e.getMessage());
		}
	}
	
	
	public String getInstance()
	{
		return instance;
	}
	public String getType()
	{
		return type;
	}
	
	
	public void setInstance(String instance)
	{
		this.instance = instance;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	

}
