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

import org.junit.Test;

import org.springframework.boot.actuate.endpoint.EnvironmentEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for generating documentation describing the {@link EnvironmentEndpoint}.
 *
 * @author Andy Wilkinson
 */
public class EnvironmentEndpointDocumentationTests
		extends AbstractEndpointDocumentationTests {

	@Test
	public void env() throws Exception {
		this.mockMvc.perform(get("/application/env")).andExpect(status().isOk())
				.andDo(MockMvcRestDocumentation.document("env", responseFields(
						fieldWithPath("activeProfiles").description("Active profiles"),
						fieldWithPath("propertySources")
								.description("Property sources in order of precedence"),
						fieldWithPath("propertySources.[].name")
								.description("Name of the property source"),
						fieldWithPath("propertySources.[].properties").description(
								"Properties in the property source keyed by property name"),
						fieldWithPath("propertySources.[].properties.*.value")
								.description("Value of the property"),
						fieldWithPath("propertySources.[].properties.*.origin")
								.description("Origin of the property"))));
	}

	@Configuration
	@Import(BaseDocumentationConfiguration.class)
	static class TestConfiguration {

		@Bean
		public EnvironmentEndpoint endpoint(Environment environment) {
			return new EnvironmentEndpoint(environment);
		}

	}

}
