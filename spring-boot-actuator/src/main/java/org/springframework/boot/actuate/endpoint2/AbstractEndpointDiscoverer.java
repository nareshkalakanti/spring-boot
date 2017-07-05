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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;

/**
 * Base class for endpoint discoverer implementations.
 *
 * @param <T> the {@link EndpointOperationInfo} type
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public abstract class AbstractEndpointDiscoverer<T extends EndpointOperationInfo> {

	private final Class<? extends Annotation> annotationType;

	private final ApplicationContext applicationContext;

	private final OperationInfoFactory<T> operationInfoFactory;

	/**
	 * Creates a new {@link AbstractEndpointDiscoverer} that will discover endpoints
	 * annotated with the given {@code annotationType} in the given
	 * {@code applicationContext}.
	 *
	 * @param annotationType the type of the annotation used to identify endpoint beans
	 * @param applicationContext the application context to examine
	 * @param operationInfoFactory the factory used to create the descriptions of the
	 * endpoint's operations
	 */
	protected AbstractEndpointDiscoverer(Class<? extends Annotation> annotationType,
			ApplicationContext applicationContext,
			OperationInfoFactory<T> operationInfoFactory) {
		this.annotationType = annotationType;
		this.applicationContext = applicationContext;
		this.operationInfoFactory = operationInfoFactory;
	}

	/**
	 * Perform endpoint discovery.
	 *
	 * @return the list of {@link EndpointInfo EndpointInfos} that describes the
	 * discovered endpoints
	 */
	public List<EndpointInfo<T>> discoverEndpoints() {
		String[] endpointBeanNames = this.applicationContext
				.getBeanNamesForAnnotation(this.annotationType);
		return Stream.of(endpointBeanNames).map((beanName) -> {
			Class<?> beanType = this.applicationContext.getType(beanName);
			AnnotationAttributes endpointAttributes = AnnotatedElementUtils
					.getMergedAnnotationAttributes(beanType, this.annotationType);
			Endpoint endpoint = AnnotatedElementUtils.findMergedAnnotation(beanType,
					Endpoint.class);
			Map<Method, T> operationMethods = MethodIntrospector.selectMethods(beanType,
					(MetadataLookup<T>) (method) -> {
				AnnotationAttributes operationAttributes = AnnotatedElementUtils
						.getMergedAnnotationAttributes(method, EndpointOperation.class);
				return this.operationInfoFactory.createOperationInfo(endpointAttributes,
						operationAttributes, beanName, method);
			});

			return new EndpointInfo<T>(endpoint.id(), operationMethods.values());
		}).collect(Collectors.toList());
	}

	/**
	 * An {@code OperationInfoFactory} creates an {@link EndpointOperationInfo} that
	 * describes an operation on an endpoint.
	 * @param <T> the {@link EndpointOperationInfo} type
	 */
	@FunctionalInterface
	protected interface OperationInfoFactory<T extends EndpointOperationInfo> {

		/**
		 * Creates an operation info to describe an operation on an endpoint.
		 * @param endpointAttributes the annotation attributes for the endpoint
		 * @param operationAttributes the annotation attributes for the operation
		 * @param beanName the name of the endpoint bean
		 * @param operationMethod the method on the bean that implements the operation
		 * @return the operation info that describes the operation
		 */
		T createOperationInfo(AnnotationAttributes endpointAttributes,
				AnnotationAttributes operationAttributes, String beanName,
				Method operationMethod);

	}

}
