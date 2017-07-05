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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.endpoint2.AbstractEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.Endpoint;
import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;
import org.springframework.context.ApplicationContext;

/**
 * Discovers the {@link WebEndpoint web endpoints} in an {@link ApplicationContext}. Web
 * endpoints include all {@link Endpoint standard endpoints} and any {@link WebEndpoint
 * web-specific} additions and overrides.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class WebEndpointDiscoverer
		extends AbstractEndpointDiscoverer<WebEndpointOperationInfo> {

	private final EndpointDiscoverer endpointDiscoverer;

	/**
	 * Creates a new {@link WebEndpointDiscoverer} that will discover {@link WebEndpoint
	 * web endpoints} in the given {@code applicationContext}.
	 *
	 * @param endpointDiscoverer the discoverer for standard endpoints that will be
	 * combined with any web-specific endpoints
	 * @param applicationContext the application context
	 */
	WebEndpointDiscoverer(EndpointDiscoverer endpointDiscoverer,
			ApplicationContext applicationContext) {
		super(WebEndpoint.class, applicationContext,
				(endpointAttributes, operationAttributes, beanName, method) -> {
					WebEndpointOperationInfo operationInfo = new WebEndpointOperationInfo(
							endpointAttributes.getString("id"), beanName, method,
							operationAttributes.getEnum("type"));
					return operationInfo;
				});
		this.endpointDiscoverer = endpointDiscoverer;
	}

	@Override
	public List<EndpointInfo<WebEndpointOperationInfo>> discoverEndpoints() {
		List<EndpointInfo<EndpointOperationInfo>> standardEndpoints = this.endpointDiscoverer
				.discoverEndpoints();
		List<EndpointInfo<WebEndpointOperationInfo>> webEndpoints = super.discoverEndpoints();
		return merge(standardEndpoints.stream().map(this::convert)
				.collect(Collectors.toList()), webEndpoints);
	}

	/**
	 * Merges two lists of {@link EndpointInfo EndpointInfos} into one. When a base
	 * endpoint has the same id as an overriding endpoint, an operation on the overriding
	 * endpoint will override an operation on the base endpoint with the same type.
	 * @param baseEndpoints the base endpoints
	 * @param overridingEndpoints the overriding endpoints
	 * @return the merged list of endpoints
	 */
	private List<EndpointInfo<WebEndpointOperationInfo>> merge(
			List<EndpointInfo<WebEndpointOperationInfo>> baseEndpoints,
			List<EndpointInfo<WebEndpointOperationInfo>> overridingEndpoints) {
		Map<String, EndpointInfo<WebEndpointOperationInfo>> endpointsById = new HashMap<>();
		for (EndpointInfo<WebEndpointOperationInfo> standardEndpoint : baseEndpoints) {
			endpointsById.put(standardEndpoint.getId(), standardEndpoint);
		}
		for (EndpointInfo<WebEndpointOperationInfo> webEndpoint : overridingEndpoints) {
			endpointsById.merge(webEndpoint.getId(), webEndpoint, this::merge);
		}
		return new ArrayList<>(endpointsById.values());
	}

	/**
	 * Convert an {@link EndpointOperationInfo} into a {@link WebEndpointOperationInfo}.
	 * @param endpointInfo the {@code EndpointOperationInfo} to convert
	 * @return the {@code WebEndpointOperationInfo}
	 */
	private EndpointInfo<WebEndpointOperationInfo> convert(
			EndpointInfo<EndpointOperationInfo> endpointInfo) {
		List<WebEndpointOperationInfo> webOperations = new ArrayList<>();
		endpointInfo.getOperations().forEach((type, operationInfo) -> {
			webOperations.add(new WebEndpointOperationInfo(endpointInfo.getId(),
					operationInfo.getBeanName(), operationInfo.getOperationMethod(),
					operationInfo.getType()));
		});
		return new EndpointInfo<WebEndpointOperationInfo>(endpointInfo.getId(),
				webOperations);
	}

	/**
	 * Merges two {@link EndpointInfo EndpointInfos} into a single {@code EndpointInfo}.
	 * When the two endpoints have an operation with the same {@link EndpointOperationType
	 * type}, the operation on the {@code baseEndpoint} is overridden by the operation on
	 * the {@code overridingEndpoint}.
	 * @param baseEndpoint the base endpoint
	 * @param overridingEndpoint the overriding endpoint
	 * @return the merged endpoint
	 */
	private EndpointInfo<WebEndpointOperationInfo> merge(
			EndpointInfo<WebEndpointOperationInfo> baseEndpoint,
			EndpointInfo<WebEndpointOperationInfo> overridingEndpoint) {
		Map<EndpointOperationType, WebEndpointOperationInfo> operations = new HashMap<>(
				baseEndpoint.getOperations());
		operations.putAll(overridingEndpoint.getOperations());
		return new EndpointInfo<WebEndpointOperationInfo>(baseEndpoint.getId(),
				operations.values());
	}

}
