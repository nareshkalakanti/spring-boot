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

package org.springframework.boot.test.rule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * A JUnit {@link TestRule} that can be used as a {@link Resource}. This resource
 * delegates to another {@link Resource} that is determined by the currently executing
 * test method.
 * <p>
 * Resources are located using the pattern
 * {@code classpath*:/fully/qualified/test/class/name-methodName.*}. For example within a
 * test method named {@code getIndex} in {@code com.example.FooClientTests} resources will
 * be loaded from the class path using {@code /com/example/FooClientTests-getIndex.*}.
 *
 * @author Andy Wilkinson
 */
public class TestMethodResource implements Resource, TestRule {

	private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

	private Resource delegate;

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				TestMethodResource.this.delegate = resolveResource(
						getResourceLocationPattern(description.getTestClass(),
								description.getMethodName()));
				base.evaluate();
			}

		};
	}

	/**
	 * Returns the location pattern that should be used to resolve resources for the given
	 * {@code testClass} and {@code testMethodName}.
	 *
	 * @param testClass the current test class
	 * @param testMethodName the name of the current test method
	 * @return the location pattern
	 */
	protected String getResourceLocationPattern(Class<?> testClass,
			String testMethodName) {
		return ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/"
				+ testClass.getName().replace('.', '/') + "-" + testMethodName + ".*";
	}

	private Resource resolveResource(String locationPattern) {
		try {
			Resource[] resources = this.resourceResolver.getResources(locationPattern);
			return selectResource(resources);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to resolve response resources", ex);
		}
	}

	/**
	 * Selects the resource to use from the given {@code candidates}. If the array
	 * contains a single element, that element is returned. If the array is empty,
	 * {@code null} is returned. If the array contains multiple elements an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param candidates the resources to select from
	 * @return the selected resource
	 * @throws IllegalStateException if there were multiple candidates
	 */
	protected Resource selectResource(Resource[] candidates) {
		if (candidates.length == 1) {
			return candidates[0];
		}
		if (candidates.length == 0) {
			return null;
		}
		throw new IllegalStateException("Multiple resources were found");
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.delegate.getInputStream();
	}

	@Override
	public boolean exists() {
		return this.delegate.exists();
	}

	@Override
	public boolean isReadable() {
		return this.delegate.isReadable();
	}

	@Override
	public boolean isOpen() {
		return this.delegate.isOpen();
	}

	@Override
	public URL getURL() throws IOException {
		return this.delegate.getURL();
	}

	@Override
	public URI getURI() throws IOException {
		return this.delegate.getURI();
	}

	@Override
	public File getFile() throws IOException {
		return this.delegate.getFile();
	}

	@Override
	public long contentLength() throws IOException {
		return this.delegate.contentLength();
	}

	@Override
	public long lastModified() throws IOException {
		return this.delegate.lastModified();
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return this.delegate.createRelative(relativePath);
	}

	@Override
	public String getFilename() {
		return this.delegate.getFilename();
	}

	@Override
	public String getDescription() {
		return this.delegate.getDescription();
	}

}
