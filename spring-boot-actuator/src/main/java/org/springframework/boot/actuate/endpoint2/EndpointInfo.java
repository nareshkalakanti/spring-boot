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

package org.springframework.boot.actuate.endpoint2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author awilkinson
 */
public class EndpointInfo {

	private final String beanName;

	private final String id;

	private final Map<EndpointOperationType, EndpointOperationInfo> operations = new HashMap<>();

	public EndpointInfo(String beanName, String id,
			Collection<EndpointOperationInfo> operations) {
		this.beanName = beanName;
		this.id = id;
		for (EndpointOperationInfo operation : operations) {
			EndpointOperationInfo previous = this.operations
					.putIfAbsent(operation.getType(), operation);
			if (previous != null) {
				throw new IllegalStateException("Found multiple operations of type "
						+ operation.getType() + ": " + previous + " and " + operation);
			}
		}
	}

	public String getBeanName() {
		return this.beanName;
	}

	public String getId() {
		return this.id;
	}

	public Map<EndpointOperationType, EndpointOperationInfo> getOperations() {
		return this.operations;
	}

}
