/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.endpoint2.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint2.EndpointOperation;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;
import org.springframework.boot.actuate.endpoint2.HealthEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * A web specialization of {@link HealthEndpoint}.
 *
 * @author Andy Wilkinson
 */
@WebEndpoint(id = "health")
class HealthWebEndpoint {

	private final HealthEndpoint delegate;

	private final Map<String, HttpStatus> statusMapping = new HashMap<>();

	HealthWebEndpoint(HealthEndpoint delegate) {
		this.delegate = delegate;
		this.statusMapping.put(Status.DOWN.getCode(), HttpStatus.SERVICE_UNAVAILABLE);
		this.statusMapping.put(Status.OUT_OF_SERVICE.getCode(),
				HttpStatus.SERVICE_UNAVAILABLE);
	}

	@EndpointOperation(type = EndpointOperationType.READ)
	public ResponseEntity<Health> health() {
		Health health = this.delegate.invoke();
		return new ResponseEntity<Health>(health, getStatus(health));
	}

	private HttpStatus getStatus(Health health) {
		String code = getUniformValue(health.getStatus().getCode());
		if (code != null) {
			return this.statusMapping.keySet().stream()
					.filter((key) -> code.equals(getUniformValue(key)))
					.map(this.statusMapping::get).findFirst().orElse(HttpStatus.OK);
		}
		return null;
	}

	private String getUniformValue(String code) {
		if (code == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (char ch : code.toCharArray()) {
			if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
				builder.append(Character.toLowerCase(ch));
			}
		}
		return builder.toString();
	}

}
