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

import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;
import org.springframework.http.HttpMethod;

/**
 * Information describing an operation on a web endpoint.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public class WebEndpointOperationInfo extends EndpointOperationInfo {

	private final String endpointId;

	public WebEndpointOperationInfo(String endpointId, String beanName, Method method,
			EndpointOperationType type) {
		super(beanName, method, type);
		this.endpointId = endpointId;
	}

	/**
	 * Returns the path to which requests that invoke the operation should be sent.
	 * @return the path
	 */
	public String getPath() {
		String path = "/" + this.endpointId;
		if (getType() == EndpointOperationType.PARTIAL_READ) {
			path += "/{selector}";
		}
		return path;
	}

	/**
	 * Returns the HTTP method which requests that invoke the operation should use.
	 * @return the HTTP method
	 */
	public HttpMethod getHttpMethod() {
		if (getType() == EndpointOperationType.WRITE) {
			return HttpMethod.POST;
		}
		return HttpMethod.GET;
	}

}
