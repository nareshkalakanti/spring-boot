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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperation;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * Discovers the {@link WebEndpoint web endpoints} in an {@link ApplicationContext}.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class WebEndpointDiscoverer {

	private final ApplicationContext applicationContext;

	private final EndpointDiscoverer endpointDiscoverer;

	/**
	 * Creates a new {@link WebEndpointDiscoverer} that will discover {@link WebEndpoint
	 * web endpoints} in the given {@code applicationContext}.
	 *
	 * @param endpointDiscoverer the discoverer for standard endpoints that will be
	 * combined with any web-specific endpoints
	 * @param applicationContext the application context
	 */
	public WebEndpointDiscoverer(EndpointDiscoverer endpointDiscoverer,
			ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.endpointDiscoverer = endpointDiscoverer;
	}

	public List<EndpointInfo> discoverEndpoints() {
		List<EndpointInfo> standardEndpoints = this.endpointDiscoverer
				.discoverEndpoints();
		List<EndpointInfo> webEndpoints = discoverWebEndpoints();
		return merge(standardEndpoints, webEndpoints);
	}

	private List<EndpointInfo> discoverWebEndpoints() {
		String[] endpointBeanNames = this.applicationContext
				.getBeanNamesForAnnotation(WebEndpoint.class);
		return Stream.of(endpointBeanNames).map((beanName) -> {
			Class<?> beanType = this.applicationContext.getType(beanName);
			WebEndpoint endpoint = AnnotatedElementUtils.findMergedAnnotation(beanType,
					WebEndpoint.class);
			Map<Method, EndpointOperationInfo> operationMethods = MethodIntrospector
					.selectMethods(beanType,
							(MetadataLookup<EndpointOperationInfo>) (
									method) -> createEndpointOperationInfo(beanName,
											method));
			return new EndpointInfo(endpoint.id(), operationMethods.values());
		}).collect(Collectors.toList());
	}

	private EndpointOperationInfo createEndpointOperationInfo(String beanName,
			Method method) {
		EndpointOperation endpointOperation = AnnotatedElementUtils
				.findMergedAnnotation(method, EndpointOperation.class);
		if (endpointOperation == null) {
			return null;
		}
		return new EndpointOperationInfo(beanName, method, endpointOperation.type());
	}

	private List<EndpointInfo> merge(List<EndpointInfo> standardEndpoints,
			List<EndpointInfo> webEndpoints) {
		Map<String, EndpointInfo> endpointsById = new HashMap<>();
		for (EndpointInfo standardEndpoint : standardEndpoints) {
			endpointsById.put(standardEndpoint.getId(), standardEndpoint);
		}
		for (EndpointInfo webEndpoint : webEndpoints) {
			endpointsById.merge(webEndpoint.getId(), webEndpoint, this::merge);
		}
		return new ArrayList<>(endpointsById.values());
	}

	private EndpointInfo merge(EndpointInfo standardEndpoint, EndpointInfo webEndpoint) {
		Map<EndpointOperationType, EndpointOperationInfo> operations = new HashMap<>(
				standardEndpoint.getOperations());
		operations.putAll(webEndpoint.getOperations());
		return new EndpointInfo(standardEndpoint.getId(), operations.values());
	}

}
