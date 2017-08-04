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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointInfrastructureAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.infrastructure.EndpointServletWebAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Abstract base class for tests that generate endpoint documentation using Spring REST
 * Docs.
 *
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.jackson.serialization.indent_output=true")
public class AbstractEndpointDocumentationTests {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	protected MockMvc mockMvc;

	@Autowired
	private WebApplicationContext applicationContext;

	@Before
	public void before() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.applicationContext)
				.apply(MockMvcRestDocumentation
						.documentationConfiguration(this.restDocumentation).uris())
				.build();
	}

	protected String describeEnumValues(Class<? extends Enum<?>> enumType) {
		return StringUtils
				.collectionToCommaDelimitedString(Stream.of(enumType.getEnumConstants())
						.map((constant) -> "`" + constant.name() + "`")
						.collect(Collectors.toList()));
	}

	@Configuration
	@Import({ JacksonAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class, WebMvcAutoConfiguration.class,
			DispatcherServletAutoConfiguration.class,
			EndpointInfrastructureAutoConfiguration.class,
			EndpointServletWebAutoConfiguration.class })
	static class BaseDocumentationConfiguration {

	}

}
