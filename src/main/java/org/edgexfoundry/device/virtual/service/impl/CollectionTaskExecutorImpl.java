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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.edgexfoundry.controller.EventClient;
import org.edgexfoundry.controller.ScheduleClient;
import org.edgexfoundry.device.virtual.domain.VirtualResource;
import org.edgexfoundry.device.virtual.service.ApplicationInitializer;
import org.edgexfoundry.device.virtual.service.CollectionTaskExecutor;
import org.edgexfoundry.device.virtual.service.VirtualResourceManager;
import org.edgexfoundry.domain.core.Event;
import org.edgexfoundry.domain.core.Reading;
import org.edgexfoundry.domain.meta.Schedule;
import org.edgexfoundry.exception.controller.ServiceException;

@Service
public class CollectionTaskExecutorImpl implements CollectionTaskExecutor {

	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = org.edgexfoundry.support.logging.client.EdgeXLoggerFactory
			.getEdgeXLogger(this.getClass());

	@Autowired
	private VirtualResourceManager virtualResourceManager;

	@Autowired
	private EventClient eventClient;

	@Autowired
	private ScheduleClient scheduleClient;
	
	@Autowired
	private ApplicationInitializer appInitializer;

	@Override
	@Async
	public void executeCollectionTask(VirtualResource vr) {
		if (vr == null)
			return;

		confirmAppInited();
		
		logger.debug("start collecting resource: " + vr.getResource() + " from Device: " + vr.getDeviceName());
		virtualResourceManager.regenerateValue(vr);

		List<Reading> readingList = new ArrayList<>();
		Reading reading = new Reading();
		reading.setName(vr.getResource());
		reading.setValue(vr.getValue());
		reading.setOrigin(System.currentTimeMillis());
		readingList.add(reading);

		Event event = new Event(vr.getDeviceName(), readingList);
		event.setOrigin(System.currentTimeMillis());

		eventClient.add(event);
		logger.debug("sent event: " + event.toString() + " to coredata");
		
		updateCollectionSchedule(vr);
	}
	
	private void confirmAppInited() {
		try {
			while (true) {
				if (appInitializer.isInit()) 
					return;
				logger.info("This Micro Service hasn't initialized completedly, wait for 3 seconds");
				Thread.sleep(3000L);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServiceException(e);
		}
	}

	private void updateCollectionSchedule(VirtualResource vr) {
		try {
			String scheduleName = "interval-for-vr-" + String.valueOf(vr.getResourceId());
			Schedule schedule = scheduleClient.scheduleForName(scheduleName);
			String frequencyString = "PT" + String.valueOf(vr.getCollectionFrequency()) + "S";
			if (frequencyString.equals(schedule.getFrequency()))
				return;  // no need to update
			
			schedule.setFrequency(frequencyString);
			logger.debug("updating collecting scheduler: " + schedule.toString());			
			scheduleClient.update(schedule);
		} catch (Exception e) {
			logger.error("update collecting scheduler failed on VirtualResource: " + vr.toString());
			logger.error(e.getMessage());
			throw new ServiceException(e);
		}
	}

}
