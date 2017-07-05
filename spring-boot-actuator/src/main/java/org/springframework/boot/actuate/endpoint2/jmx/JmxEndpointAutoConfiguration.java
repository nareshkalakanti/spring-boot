package org.springframework.boot.actuate.endpoint2.jmx;

import javax.management.MBeanServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportProperties;
import org.springframework.boot.actuate.endpoint2.EndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint2.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint2.EndpointInfo;
import org.springframework.boot.actuate.endpoint2.jmx.JmxEndpointAutoConfiguration.JmxEnabledCondition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-configuration} to enable JMX export for
 * {@link EndpointInfo endpoints}.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@Configuration
@Conditional(JmxEnabledCondition.class)
@AutoConfigureAfter({ EndpointAutoConfiguration.class, JmxAutoConfiguration.class })
public class JmxEndpointAutoConfiguration {

	private final EndpointMBeanExportProperties properties;

	private final ObjectMapper objectMapper;

	private final MBeanServer mBeanServer;

	public JmxEndpointAutoConfiguration(EndpointMBeanExportProperties properties,
			ObjectProvider<ObjectMapper> objectMapper,
			ObjectProvider<MBeanServer> mBeanServer) {
		this.properties = properties;
		this.objectMapper = objectMapper.getIfAvailable();
		this.mBeanServer = mBeanServer.getIfAvailable();
	}

	@Bean
	@ConditionalOnBean(EndpointDiscoverer.class)
	public EndpointMBeanExporter endpointMBeanExporter2(
			EndpointDiscoverer endpointDiscoverer) {
		EndpointMBeanExporter mbeanExporter = new EndpointMBeanExporter(
				endpointDiscoverer, this.objectMapper);
		String domain = this.properties.getDomain();
		if (StringUtils.hasText(domain)) {
			mbeanExporter.setDomain(domain);
		}
		mbeanExporter.setServer(this.mBeanServer);
		mbeanExporter.setEnsureUniqueRuntimeObjectNames(this.properties.isUniqueNames());
		mbeanExporter.setObjectNameStaticProperties(this.properties.getStaticNames());
		return mbeanExporter;
	}


	/**
	 * Condition to check that spring.jmx and endpoints.jmx are enabled.
	 */
	static class JmxEnabledCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context,
				AnnotatedTypeMetadata metadata) {
			boolean jmxEnabled = context.getEnvironment()
					.getProperty("spring.jmx.enabled", Boolean.class, true);
			boolean jmxEndpointsEnabled = context.getEnvironment()
					.getProperty("endpoints.jmx.enabled", Boolean.class, true);
			if (jmxEnabled && jmxEndpointsEnabled) {
				return ConditionOutcome.match(
						ConditionMessage.forCondition("JMX Enabled").found("properties")
								.items("spring.jmx.enabled", "endpoints.jmx.enabled"));
			}
			return ConditionOutcome.noMatch(ConditionMessage.forCondition("JMX Enabled")
					.because("spring.jmx.enabled or endpoints.jmx.enabled is not set"));
		}

	}
}
