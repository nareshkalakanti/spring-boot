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

import org.springframework.boot.actuate.endpoint2.EndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author awilkinson
 */
@ConditionalOnWebApplication
@Configuration
@AutoConfigureAfter(EndpointAutoConfiguration.class)
public class WebEndpointAutoConfiguration {

	@Bean
	public WebEndpointDiscoverer webEndpointDiscoverer(
			EndpointDiscoverer endpointDiscoverer,
			ApplicationContext applicationContext) {
		return new WebEndpointDiscoverer(endpointDiscoverer, applicationContext);
	}

	@Bean
	public HealthWebEndpoint healthWebEndpoint(HealthEndpoint delegate) {
		return new HealthWebEndpoint(delegate);
	}

}
