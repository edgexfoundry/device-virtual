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

import java.math.BigDecimal;
import java.util.Random;

import org.springframework.stereotype.Service;

import org.edgexfoundry.device.virtual.domain.VirtualResource;
import org.edgexfoundry.device.virtual.service.ReadValueGenerator;
import org.edgexfoundry.domain.common.IoTType;

@Service
public class ReadValueRandomGenerator implements ReadValueGenerator {

	private final org.edgexfoundry.support.logging.client.EdgeXLogger logger = org.edgexfoundry.support.logging.client.EdgeXLoggerFactory
			.getEdgeXLogger(this.getClass());

	private final static float MAX_FLOAT_VALUE = 100.00f;
	private final static float MIN_FLOAT_VALUE = 0.00f;
	private final static Random random = new Random(System.currentTimeMillis());

	@Override
	public String generateValue(VirtualResource vr) {
		IoTType type = vr.getType();

		if (type == IoTType.B) {
			return generateRandomBooleanValue();
		} else if (type == IoTType.F) {
			if (vr.getMax() != null && vr.getMin() != null) {
				try {
					return generateRandomFloatValue(Float.parseFloat(vr.getMin().toString()),
							Float.parseFloat(vr.getMax().toString()));
				} catch (NumberFormatException e) {
					logger.warn("The Max or Min format of VirtualResource are not correct: " + vr.toString());
					return generateRandomFloatValue(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE);
				}
			} else {
				return generateRandomFloatValue(MIN_FLOAT_VALUE, MAX_FLOAT_VALUE);
			}
		} else {
			return "";
		}
	}

	private String generateRandomFloatValue(float min, float max) {
		float f = (random.nextFloat() * (max - min)) + min;
		BigDecimal b = new BigDecimal(String.valueOf(f));
		return b.setScale(2, BigDecimal.ROUND_FLOOR).toString();
	}

	private String generateRandomBooleanValue() {
		return String.valueOf(random.nextBoolean());
	}

}
