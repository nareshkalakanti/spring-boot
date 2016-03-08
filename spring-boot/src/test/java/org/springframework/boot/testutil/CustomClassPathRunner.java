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

package org.springframework.boot.testutil;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import org.springframework.util.AntPathMatcher;

/**
 * A custom {@link BlockJUnit4ClassRunner} that runs tests using a customized class path.
 * A class loader is created with the customized class path and is used both to load the
 * test class and as the thread context class loader while the test is being run.
 * <p>
 * Entries can be hidden from the class path via a static {@code List<String>} field named
 * {@code hiddenEntries}. Each entry in the list is an Ant-style pattern that matches the
 * name of an entry on the classpath. For example, to exclude Hibernate Validator from the
 * classpath, {@code "hibernate-validator-*.jar"} can be used.
 *
 * @author Andy Wilkinson
 */
public class CustomClassPathRunner extends BlockJUnit4ClassRunner {

	public CustomClassPathRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected TestClass createTestClass(Class<?> testClass) {
		try {
			final ClassLoader classLoader = createTestClassLoader(testClass);
			return new TestClass(classLoader.loadClass(testClass.getName())) {

				@SuppressWarnings("unchecked")
				@Override
				public List<FrameworkMethod> getAnnotatedMethods(
						Class<? extends Annotation> annotationClass) {
					List<FrameworkMethod> methods = new ArrayList<FrameworkMethod>();
					try {
						for (FrameworkMethod frameworkMethod : super.getAnnotatedMethods(
								(Class<? extends Annotation>) classLoader
										.loadClass(annotationClass.getName()))) {
							methods.add(new CustomTcclFrameworkMethod(classLoader,
									frameworkMethod.getMethod()));
						}
						return methods;
					}
					catch (ClassNotFoundException ex) {
						throw new RuntimeException(ex);
					}
				}

			};
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private URLClassLoader createTestClassLoader(Class<?> testClass) throws Exception {
		URLClassLoader classLoader = (URLClassLoader) this.getClass().getClassLoader();
		return new URLClassLoader(filterUrls(classLoader.getURLs(), testClass),
				classLoader.getParent());
	}

	private URL[] filterUrls(URL[] urls, Class<?> testClass) throws Exception {
		ClassPathEntryFilter filter = new ClassPathEntryFilter(testClass);
		List<URL> filteredUrls = new ArrayList<URL>();
		for (URL url : urls) {
			if (!filter.isHidden(url)) {
				filteredUrls.add(url);
			}
		}
		return filteredUrls.toArray(new URL[filteredUrls.size()]);
	}

	private static class ClassPathEntryFilter {

		private final List<String> hiddenEntries;

		private final AntPathMatcher matcher = new AntPathMatcher();

		@SuppressWarnings("unchecked")
		private ClassPathEntryFilter(Class<?> testClass) throws Exception {
			this.hiddenEntries = (List<String>) testClass.getField("hiddenEntries")
					.get(null);
		}

		private boolean isHidden(URL url) throws Exception {
			if (!"file".equals(url.getProtocol())) {
				return false;
			}
			String name = new File(url.toURI()).getName();
			for (String hiddenEntry : this.hiddenEntries) {
				if (this.matcher.match(hiddenEntry, name)) {
					return true;
				}
			}
			return false;
		}
	}

	private static class CustomTcclFrameworkMethod extends FrameworkMethod {

		private final ClassLoader customTccl;

		public CustomTcclFrameworkMethod(ClassLoader customTccl, Method method) {
			super(method);
			this.customTccl = customTccl;
		}

		@Override
		public Object invokeExplosively(Object target, Object... params)
				throws Throwable {
			ClassLoader originalTccl = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(this.customTccl);
			try {
				return super.invokeExplosively(target, params);
			}
			finally {
				Thread.currentThread().setContextClassLoader(originalTccl);
			}
		}

	}

}
