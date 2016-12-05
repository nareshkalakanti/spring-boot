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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.util.StringUtils;

/**
 * A JUnit rule that provides an alternative to {@link MockRestResponseCreators}, using
 * the name of the current test class and method to automatically configure the body of
 * created responses.
 * <p>
 * By default, responses are located using the pattern
 * {@code classpath*:/fully/qualified/test/class/name-methodName.*}. For example within a
 * test method named {@code getIndex} in {@code com.example.FooClientTests} resources will
 * be loaded from {@code /com/example/FooClientTests/getIndex.*}.
 *
 * @author Andy Wilkinson
 * @since 1.5.0
 */
public class TestMethodResponseCreators implements TestRule {

	private static final Map<String, MediaType> CONTENT_TYPES;

	static {
		Map<String, MediaType> contentTypes = new HashMap<String, MediaType>();
		contentTypes.put("json", MediaType.APPLICATION_JSON);
		contentTypes.put("xml", MediaType.APPLICATION_XML);
		CONTENT_TYPES = Collections.unmodifiableMap(contentTypes);
	}

	private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

	private String resourcePattern;

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				TestMethodResponseCreators.this.configureResourcePattern(
						description.getTestClass(), description.getMethodName());
				base.evaluate();
			}
		};
	}

	/**
	 * Returns a {@link DefaultResponseCreator} with a pre-configured body and a
	 * {@code 200 OK} response status.
	 *
	 * @return the response creator for further customization
	 */
	public DefaultResponseCreator withSuccess() {
		return withStatus(HttpStatus.OK);
	}

	/**
	 * Returns a {@link DefaultResponseCreator} with a pre-configured body and the given
	 * {@code status}.
	 *
	 * @param status the status of the created response
	 * @return the response creator for further customization
	 */
	public DefaultResponseCreator withStatus(HttpStatus status) {
		Resource resource = resolveResource();
		DefaultResponseCreator responseCreator = MockRestResponseCreators
				.withStatus(status);
		if (resource != null) {
			responseCreator.body(resource);
			responseCreator.contentType(determineContentType(resource));
		}
		return responseCreator;
	}

	protected void configureResourcePattern(Class<?> testClass, String testMethodName) {
		this.resourcePattern = "classpath*:/" + testClass.getName().replace('.', '/')
				+ "-" + testMethodName + ".*";
	}

	private Resource resolveResource() {
		try {
			Resource[] resources = this.resourceResolver
					.getResources(this.resourcePattern);
			return selectResource(resources);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to resolve response resources", ex);
		}
	}

	protected Resource selectResource(Resource[] resources) {
		if (resources.length == 1) {
			return resources[0];
		}
		if (resources.length == 0) {
			return null;
		}
		throw new IllegalStateException(
				"Multiple resources matched the pattern '" + this.resourcePattern + "'");
	}

	protected MediaType determineContentType(Resource resource) {
		return CONTENT_TYPES
				.get(StringUtils.getFilenameExtension(resource.getFilename()));
	}

}
