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

package org.springframework.boot.test.autoconfigure.security;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;

/**
 * {@link MockMvcBuilderCustomizer} for applying Spring Security.
 *
 * @see SecurityMockMvcConfigurers#springSecurity()
 * @author Andy Wilkinson
 */
class SecurityMockMvcBuilderCustomizer implements MockMvcBuilderCustomizer {

	@Override
	public void customize(ConfigurableMockMvcBuilder<?> builder) {
		builder.apply(SecurityMockMvcConfigurers.springSecurity());
	}

}
