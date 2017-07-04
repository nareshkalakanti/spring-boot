package org.springframework.boot.actuate.endpoint2.webflux;

import reactor.core.publisher.Flux;

import org.springframework.boot.actuate.endpoint2.EndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@ConditionalOnClass({ Flux.class, WebFluxConfigurer.class})
@AutoConfigureAfter(EndpointAutoConfiguration.class)
@Configuration
@Import(WebFluxEndpointRoutes.class)
public class WebFluxEndpointAutoConfiguration {

}
