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

package org.springframework.boot.actuate.endpoint2.webflux;

import reactor.core.publisher.Flux;

import org.springframework.boot.actuate.endpoint2.Endpoint;
import org.springframework.boot.actuate.endpoint2.EndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Auto-configuration for actuator {@link Endpoint Endpoints} that are made accessible
 * over HTTP using WebFlux.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@ConditionalOnClass({ Flux.class, WebFluxConfigurer.class })
@AutoConfigureAfter(EndpointAutoConfiguration.class)
@Configuration
@Import(WebFluxEndpointRoutes.class)
public class WebFluxEndpointAutoConfiguration {

}
