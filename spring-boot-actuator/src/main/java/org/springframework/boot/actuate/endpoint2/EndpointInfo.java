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
 * Information describing an endpoint.
 *
 * @param <T> the type of the endpoint operation info
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class EndpointInfo<T extends EndpointOperationInfo> {

	private final String id;

	private final Map<EndpointOperationType, T> operations = new HashMap<>();

	/**
	 * Creates a new {@code EndpointInfo} describing an endpoint with the given {@code id}
	 * and {@code operations}.
	 *
	 * @param id the id of the endpoint
	 * @param operations the operations of the endpoint
	 */
	public EndpointInfo(String id, Collection<T> operations) {
		this.id = id;
		for (T operation : operations) {
			T previous = this.operations.putIfAbsent(operation.getType(), operation);
			if (previous != null) {
				throw new IllegalStateException("Found multiple operations of type "
						+ operation.getType() + ": " + previous + " and " + operation);
			}
		}
	}

	/**
	 * Returns the id of the endpoint.
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the operations of the endpoint, keyed by type.
	 * @return the operations
	 */
	public Map<EndpointOperationType, T> getOperations() {
		return this.operations;
	}

}
