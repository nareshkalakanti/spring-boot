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

package org.springframework.boot.actuate.endpoint2.jersey;

import java.lang.reflect.Method;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.Resource.Builder;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.endpoint2.Endpoint;
import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.util.ReflectionUtils;

/**
 * Registers any {@link Endpoint Endpoints} with Jersey.
 *
 * @author Andy Wilkinson
 */
class JerseyEndpointRegistrar implements InitializingBean {

	private final ApplicationContext applicationContext;

	private final EndpointDiscoverer endpointDiscoverer;

	private final ResourceConfig resourceConfig;

	public JerseyEndpointRegistrar(ApplicationContext applicationContext,
			EndpointDiscoverer endpointDiscoverer, ResourceConfig resourceConfig) {
		this.applicationContext = applicationContext;
		this.endpointDiscoverer = endpointDiscoverer;
		this.resourceConfig = resourceConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for (EndpointInfo endpointInfo : this.endpointDiscoverer.discoverEndpoints()) {
			Map<EndpointOperationType, EndpointOperationInfo> operations = endpointInfo
					.getOperations();
			for (EndpointOperationInfo operationInfo : operations.values()) {
				Builder resourceBuilder = Resource.builder()
						.path(getPathForOperation(endpointInfo, operationInfo));
				resourceBuilder.addMethod(getHttpMethodForOperation(operationInfo))
						.produces("application/vnd.spring-boot.actuator.v2+json")
						.handledBy(new EndpointInvokingInflector(
								this.applicationContext
										.getBean(endpointInfo.getBeanName()),
								operationInfo.getOperationMethod()));
				this.resourceConfig.registerResources(resourceBuilder.build());
			}
		}
	}

	private String getPathForOperation(EndpointInfo endpoint,
			EndpointOperationInfo operation) {
		return operation.getType() == EndpointOperationType.PARTIAL_READ
				? endpoint.getId() + "/{selector}" : endpoint.getId();
	}

	private String getHttpMethodForOperation(EndpointOperationInfo operation) {
		return operation.getType() == EndpointOperationType.WRITE ? HttpMethod.POST.name()
				: HttpMethod.GET.name();
	}

	private static class EndpointInvokingInflector
			implements Inflector<ContainerRequestContext, Object> {

		private final Object bean;

		private final Method method;

		private EndpointInvokingInflector(Object bean, Method method) {
			this.bean = bean;
			this.method = method;
		}

		@Override
		public Object apply(ContainerRequestContext data) {
			MultivaluedMap<String, String> pathParameters = data.getUriInfo()
					.getPathParameters();
			if (pathParameters.isEmpty()) {
				return ReflectionUtils.invokeMethod(this.method, this.bean);
			}
			if (pathParameters.size() == 1) {
				return ReflectionUtils.invokeMethod(this.method, this.bean,
						pathParameters.values().iterator().next().iterator().next());
			}
			throw new IllegalArgumentException("Wrong number of path parameters");
		}

	}

}
