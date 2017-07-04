package org.springframework.boot.actuate.endpoint2.webflux;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationInfo;
import org.springframework.boot.actuate.endpoint2.EndpointOperationType;
import org.springframework.boot.actuate.endpoint2.NonBlocking;
import org.springframework.boot.actuate.endpoint2.web.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.web.WebEndpointOperationInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@Configuration
public class WebFluxEndpointRoutes {

	private static final MediaType ACTUATOR_MEDIA_TYPE = MediaType
			.parseMediaType("application/vnd.spring-boot.actuator.v2+json");

	private final ApplicationContext applicationContext;

	public WebFluxEndpointRoutes(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Bean
	@ConditionalOnBean(EndpointDiscoverer.class)
	public RouterFunction<ServerResponse> webFluxEndpointRouter(
			WebEndpointDiscoverer endpointDiscoverer) {
		List<RouterFunction<ServerResponse>> routes = new ArrayList<>();
		for (EndpointInfo<WebEndpointOperationInfo> endpointInfo : endpointDiscoverer
				.discoverEndpoints()) {
			Map<EndpointOperationType, WebEndpointOperationInfo> operations = endpointInfo
					.getOperations();
			for (WebEndpointOperationInfo operationInfo : operations.values()) {
				EndpointHandler handler = createEndpointHandler(operationInfo);
				if (handler != null) {
					routes.add(route(predicate(endpointInfo, operationInfo),
							handler::apply));
				}
			}
		}
		return routes.stream().reduce(RouterFunction::and).orElse(null); // TODO: empty
																			// router?
	}

	private RequestPredicate predicate(
			EndpointInfo<WebEndpointOperationInfo> endpointInfo,
			WebEndpointOperationInfo operation) {
		return method(operation.getHttpMethod())
				.and(accept(ACTUATOR_MEDIA_TYPE).or(accept(MediaType.APPLICATION_JSON)))
				.and((path(operation.getPath())));
	}

	private EndpointHandler createEndpointHandler(EndpointOperationInfo operation) {
		Object bean = this.applicationContext.getBean(operation.getBeanName());
		Method method = operation.getOperationMethod();
		ResolvableType returnType = ResolvableType.forMethodReturnType(method);
		ResolvableType generic = returnType.as(Publisher.class).getGeneric(0);
		if (generic != ResolvableType.NONE) {
			return new EndpointHandler(bean, method, true, generic.getRawClass());
		}
		if (AnnotatedElementUtils.isAnnotated(method, NonBlocking.class)) {
			return new EndpointHandler(bean, method, false, returnType.getRawClass());
		}
		// log?
		return null;
	}

	private static class EndpointHandler {

		private final Object bean;

		private final Method method;

		private final boolean reactive;

		private final Class<?> bodyType;

		EndpointHandler(Object bean, Method method, boolean reactive, Class<?> bodyType) {
			this.bean = bean;
			this.method = method;
			this.reactive = reactive;
			this.bodyType = bodyType;
		}

		public Mono<ServerResponse> apply(ServerRequest serverRequest) {
			ServerResponse.BodyBuilder response = ServerResponse.ok()
					.contentType(ACTUATOR_MEDIA_TYPE);
			if (serverRequest.pathVariables().isEmpty()) {
				Publisher<?> result = invoke();
				return new UnsafeBody<>(this.bodyType, result).body(response);
			}
			else {
				Publisher<?> result = invoke(
						serverRequest.pathVariables().values().iterator().next());
				return new UnsafeBody<>(this.bodyType, result).body(response);
			}
		}

		private Publisher<?> invoke(Object... parameters) {
			if (this.reactive) {
				return (Publisher<?>) ReflectionUtils.invokeMethod(this.method, this.bean,
						parameters);
			}
			else {
				return Mono.defer(() -> Mono.justOrEmpty(ReflectionUtils
						.invokeMethod(this.method, this.bean, parameters)));
			}
		}

		private static class UnsafeBody<T, P extends Publisher<T>> {

			private final Class<T> type;

			private final P publisher;

			@SuppressWarnings("unchecked")
			UnsafeBody(Class<?> type, Publisher<?> publisher) {
				this.type = (Class<T>) type;
				this.publisher = (P) publisher;
			}

			Mono<ServerResponse> body(ServerResponse.BodyBuilder builder) {
				return builder.body(this.publisher, this.type);
			}

		}

	}

}
