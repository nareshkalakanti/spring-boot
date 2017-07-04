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

import org.glassfish.jersey.server.ResourceConfig;

import org.springframework.boot.actuate.endpoint2.Endpoint;
import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for actuator {@link Endpoint Endpoints} that are made accessible
 * over HTTP using Jersey.
 *
 * @author Andy Wilkinson
 */
@ConditionalOnClass(ResourceConfig.class)
@Configuration
public class JerseyEndpointAutoConfiguration {

	@ConditionalOnBean({ ResourceConfig.class, EndpointDiscoverer.class })
	@Bean
	public JerseyEndpointRegistrar jerseyEndpointRegistrar(
			ApplicationContext applicationContext, EndpointDiscoverer endpointDiscoverer,
			ResourceConfig resourceConfig) {
		return new JerseyEndpointRegistrar(applicationContext, endpointDiscoverer,
				resourceConfig);
	}

}
