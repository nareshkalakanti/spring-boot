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

package org.springframework.boot.actuate.endpoint2.mvc;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.web.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.web.WebEndpointOperationInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

/**
 * A custom {@link RequestMappingInfoHandlerMapping} that makes Actuator endpoints
 * available over HTTP using Spring MVC.
 *
 * @author Andy Wilkinson
 */
class WebEndpointHandlerMapping extends RequestMappingInfoHandlerMapping
		implements InitializingBean {

	private final WebEndpointDiscoverer endpointDiscoverer;

	private final ApplicationContext applicationContext;

	public WebEndpointHandlerMapping(ApplicationContext applicationContext,
			WebEndpointDiscoverer endpointDiscoverer) {
		this.applicationContext = applicationContext;
		this.endpointDiscoverer = endpointDiscoverer;
		setOrder(-100);
	}

	@Override
	protected void initHandlerMethods() {
		Method handle = ReflectionUtils.findMethod(EndpointHandler.class, "handle",
				HttpServletRequest.class);
		for (EndpointInfo<WebEndpointOperationInfo> endpoint : this.endpointDiscoverer
				.discoverEndpoints()) {
			for (WebEndpointOperationInfo operation : endpoint.getOperations().values()) {
				registerMapping(createRequestMappingInfo(operation),
						new EndpointHandler(
								this.applicationContext.getBean(operation.getBeanName()),
								operation.getOperationMethod()),
						handle);
			}
		}
	}

	private RequestMappingInfo createRequestMappingInfo(
			WebEndpointOperationInfo operationInfo) {
		return new RequestMappingInfo(null,
				new PatternsRequestCondition(operationInfo.getPath()),
				new RequestMethodsRequestCondition(
						RequestMethod.valueOf(operationInfo.getHttpMethod().name())),
				null, null, null, new ProducesRequestCondition(
						"application/vnd.spring-boot.actuator.v2+json"),
				null);
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return false;
	}

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method,
			Class<?> handlerType) {
		return null;
	}

	final class EndpointHandler {

		private final Object endpoint;

		private final Method operation;

		EndpointHandler(Object endpoint, Method operation) {
			this.endpoint = endpoint;
			this.operation = operation;
		}

		@SuppressWarnings("unchecked")
		@ResponseBody
		public Object handle(HttpServletRequest request) {
			Map<String, String> uriVariables = (Map<String, String>) request
					.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			if (uriVariables.isEmpty()) {
				return ReflectionUtils.invokeMethod(this.operation, this.endpoint);
			}
			else if (uriVariables.size() == 1) {
				return ReflectionUtils.invokeMethod(this.operation, this.endpoint,
						uriVariables.values().iterator().next());
			}
			else {
				throw new IllegalArgumentException("Wrong number of path parameters");
			}
		}

	}

}
