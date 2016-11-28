/*
 * Copyright 2012-2016 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure;

import javax.management.MBeanServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration.JmxEnabledCondition;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.EndpointPayloadConverter;
import org.springframework.boot.actuate.endpoint.GsonEndpointPayloadConverter;
import org.springframework.boot.actuate.endpoint.JacksonEndpointPayloadConverter;
import org.springframework.boot.actuate.endpoint.jmx.EndpointMBeanExporter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} to enable JMX export for
 * {@link Endpoint}s.
 *
 * @author Christian Dupuis
 * @author Andy Wilkinson
 */
@Configuration
@Conditional(JmxEnabledCondition.class)
@AutoConfigureAfter({ EndpointAutoConfiguration.class, JmxAutoConfiguration.class,
		JacksonAutoConfiguration.class, GsonAutoConfiguration.class })
@EnableConfigurationProperties(EndpointMBeanExportProperties.class)
public class EndpointMBeanExportAutoConfiguration {

	private final EndpointMBeanExportProperties properties;

	public EndpointMBeanExportAutoConfiguration(
			EndpointMBeanExportProperties properties) {
		this.properties = properties;
	}

	@Bean
	public EndpointMBeanExporter endpointMBeanExporter(MBeanServer server,
			EndpointPayloadConverter endpointPayloadConverter) {
		EndpointMBeanExporter mbeanExporter = new EndpointMBeanExporter(
				endpointPayloadConverter);
		String domain = this.properties.getDomain();
		if (StringUtils.hasText(domain)) {
			mbeanExporter.setDomain(domain);
		}
		mbeanExporter.setServer(server);
		mbeanExporter.setEnsureUniqueRuntimeObjectNames(this.properties.isUniqueNames());
		mbeanExporter.setObjectNameStaticProperties(this.properties.getStaticNames());
		return mbeanExporter;
	}

	@Bean
	@ConditionalOnMissingBean(MBeanServer.class)
	public MBeanServer mbeanServer() {
		return new JmxAutoConfiguration().mbeanServer();
	}

	@Configuration
	static class EndpointPayloadConverterConfiguration {

		@ConditionalOnClass(ObjectMapper.class)
		static class JacksonEndpointPayloadConverterConfiguration {

			@Bean
			public JacksonEndpointPayloadConverter jacksonEndpointPayloadConverter(
					ObjectProvider<ObjectMapper> objectMapperProvider) {
				ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
				return new JacksonEndpointPayloadConverter(
						objectMapper == null ? new ObjectMapper() : objectMapper);
			}

		}

		@ConditionalOnClass(Gson.class)
		@ConditionalOnBean(Gson.class)
		static class GsonEndpointPayloadConverterConfiguration {

			@Bean
			public GsonEndpointPayloadConverter gsonEndpointPayloadConverter(Gson gson) {
				return new GsonEndpointPayloadConverter(gson);
			}

		}

	}

	/**
	 * Condition to check that spring.jmx and endpoints.jmx are enabled.
	 */
	static class JmxEnabledCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			boolean jmxEnabled = isEnabled(context, "spring.jmx.");
			boolean jmxEndpointsEnabled = isEnabled(context, "endpoints.jmx.");
			if (jmxEnabled && jmxEndpointsEnabled) {
				return ConditionOutcome.match(
						ConditionMessage.forCondition("JMX Enabled").found("properties")
								.items("spring.jmx.enabled", "endpoints.jmx.enabled"));
			}
			return ConditionOutcome.noMatch(ConditionMessage.forCondition("JMX Enabled")
					.because("spring.jmx.enabled or endpoints.jmx.enabled is not set"));
		}

		private boolean isEnabled(ConditionContext context, String prefix) {
			RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
					context.getEnvironment(), prefix);
			return resolver.getProperty("enabled", Boolean.class, true);
		}

	}

}
