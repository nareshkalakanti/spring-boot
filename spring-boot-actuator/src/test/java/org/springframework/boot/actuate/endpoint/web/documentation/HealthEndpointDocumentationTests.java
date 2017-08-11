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

package org.springframework.boot.actuate.endpoint.web.documentation;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;

import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.DataSourceHealthIndicator;
import org.springframework.boot.actuate.health.DiskSpaceHealthIndicator;
import org.springframework.boot.actuate.health.DiskSpaceHealthIndicatorProperties;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for generating documentation describing the {@link HealthEndpoint}.
 *
 * @author Andy Wilkinson
 */
public class HealthEndpointDocumentationTests extends AbstractEndpointDocumentationTests {

	@Test
	public void health() throws Exception {
		this.mockMvc.perform(get("/application/health")).andExpect(status().isOk())
				.andDo(MockMvcRestDocumentation.document("health",
						relaxedResponseFields(
								fieldWithPath("status")
										.description("Overall status of the application"),
						fieldWithPath("details")
								.description("Details of the health of the application"),
						fieldWithPath("details.*.status").description(
								"Status of a specific part of the application"),
						fieldWithPath("details.*.details").description(
								"Details of the health of a specific part of the application"))));
	}

	@Configuration
	@Import(BaseDocumentationConfiguration.class)
	@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
	static class TestConfiguration {

		@Bean
		public HealthEndpoint endpoint(Map<String, HealthIndicator> healthIndicators) {
			return new HealthEndpoint(new OrderedHealthAggregator(), healthIndicators);
		}

		@Bean
		public DiskSpaceHealthIndicator diskSpaceHealthIndicator() {
			return new DiskSpaceHealthIndicator(new DiskSpaceHealthIndicatorProperties());
		}

		@Bean
		public DataSourceHealthIndicator dataSourceHealthIndicator(
				DataSource dataSource) {
			return new DataSourceHealthIndicator(dataSource);
		}

	}

}
