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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.junit.Test;

import org.springframework.boot.actuate.endpoint.FlywayEndpoint;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for generating documentation describing the {@link FlywayEndpoint}.
 *
 * @author Andy Wilkinson
 */
public class FlywayEndpointDocumentationTests extends AbstractEndpointDocumentationTests {

	@Test
	public void flyway() throws Exception {
		this.mockMvc.perform(get("/application/flyway")).andExpect(status().isOk())
				.andDo(MockMvcRestDocumentation.document("flyway",
						responseFields(fieldWithPath("*.migrations").description(
								"Migrations performed by the Flyway instance"))
										.andWithPrefix("*.migrations.[].",
												getMigrationFieldDescriptors())));
	}

	private List<FieldDescriptor> getMigrationFieldDescriptors() {
		return Arrays.asList(
				fieldWithPath("checksum")
						.description("Checksum of the migration. Optional").optional(),
				fieldWithPath("description")
						.description("Description of the migration. Optional").optional(),
				fieldWithPath("executionTime")
						.description(
								"Execution time in milliseconds of an applied migration")
						.optional(),
				fieldWithPath("installedBy")
						.description(
								"User that installed the applied migration. Optional")
						.optional(),
				fieldWithPath("installedOn").description(
						"Timestamp of when the applied migration was installed."
								+ " Optional")
						.optional(),
				fieldWithPath("installedRank").description(
						"Rank of the applied migration. Later migrations have higher"
								+ " ranks. Optional")
						.optional(),
				fieldWithPath("script")
						.description(
								"Name of the script used to execute the migration. Optional")
						.optional(),
				fieldWithPath("state").description("State of the migration. ("
						+ describeEnumValues(MigrationState.class) + ")"),
				fieldWithPath("type").description("Type of the migration. ("
						+ describeEnumValues(MigrationType.class) + ")"),
				fieldWithPath("version").description(
						"Version of the database after applying the migration. "
								+ "Optional")
						.optional());
	}

	@Configuration
	@Import({ BaseDocumentationConfiguration.class, EmbeddedDataSourceConfiguration.class,
			FlywayAutoConfiguration.class })
	static class TestConfiguration {

		@Bean
		public FlywayEndpoint endpoint(Map<String, Flyway> flyways) {
			return new FlywayEndpoint(flyways);
		}

	}

}
