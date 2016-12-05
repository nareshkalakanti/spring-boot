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

package org.springframework.boot.test.web.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TestMethodResponseCreators}.
 *
 * @author Andy Wilkinson
 */
public class TestMethodResponseCreatorsTests {

	@Rule
	public final TestMethodResponseCreators responseCreator = new TestMethodResponseCreators();

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void resourceResolutionUsesClassNameAndTestMethodName() throws IOException {
		ClientHttpResponse response = this.responseCreator.withSuccess()
				.createResponse(null);
		assertThat(response.getHeaders().getContentType())
				.isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8))
				.isEqualTo("{\"foo\":\"bar\"}");
	}

	@Test
	public void illegalStateExceptionWhenMultipleResourcesAreAvailable()
			throws IOException {
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("Multiple resources");
		this.responseCreator.withSuccess();
	}

}
