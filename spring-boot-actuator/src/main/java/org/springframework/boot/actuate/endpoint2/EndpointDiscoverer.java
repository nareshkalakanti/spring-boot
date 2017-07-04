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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * Discovers the {@link Endpoint Endpoints} in an {@link ApplicationContext}.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class EndpointDiscoverer {

	private final ApplicationContext applicationContext;

	/**
	 * Creates a new {@link EndpointDiscoverer} that will discover endpoints in the given
	 * {@code applicationContext}.
	 *
	 * @param applicationContext the application context
	 */
	public EndpointDiscoverer(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public List<EndpointInfo> discoverEndpoints() {
		String[] endpointBeanNames = this.applicationContext
				.getBeanNamesForAnnotation(Endpoint.class);
		return Stream.of(endpointBeanNames).map((beanName) -> {
			Class<?> beanType = this.applicationContext.getType(beanName);
			Endpoint endpoint = AnnotatedElementUtils.findMergedAnnotation(beanType,
					Endpoint.class);
			Map<Method, EndpointOperationInfo> operationMethods = MethodIntrospector
					.selectMethods(beanType, (MetadataLookup<EndpointOperationInfo>) (
							method) -> createEndpointOperationInfo(method));
			return new EndpointInfo(beanName, endpoint.id(), operationMethods.values());
		}).collect(Collectors.toList());
	}

	private EndpointOperationInfo createEndpointOperationInfo(Method method) {
		EndpointOperation endpointOperation = AnnotatedElementUtils
				.findMergedAnnotation(method, EndpointOperation.class);
		if (endpointOperation == null) {
			return null;
		}
		return new EndpointOperationInfo(method, endpointOperation.type());
	}

}
