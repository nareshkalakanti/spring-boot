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

package org.springframework.boot.autoconfigure.flyway;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FlywayProperties}.
 *
 * @author Andy Wilkinson
 */
@RunWith(Parameterized.class)
public class FlywayPropertiesTests {

	private static final List<String> IGNORED_METHODS = Arrays.asList(
			"setBaselineVersion", "setCallbacks", "setCallbacksAsClassNames",
			"setClassLoader", "setDataSource", "setIgnoreFailedFutureMigration",
			"setResolvers", "setResolversAsClassNames", "setTarget");

	private final String propertyName;

	private final Class<?> propertyType;

	@Parameters(name = "{0}")
	public static Object[] parameters() {
		List<Object[]> parameters = Arrays.asList(Flyway.class.getMethods()).stream()
				.filter((method) -> method.getName().startsWith("set"))
				.filter((method) -> method.getParameterTypes().length == 1)
				.filter((method) -> !IGNORED_METHODS.contains(method.getName()))
				.map(FlywayPropertiesTests::parametersForMethod)
				.collect(Collectors.toList());
		return parameters.toArray(new Object[parameters.size()]);
	}

	private static Object[] parametersForMethod(Method method) {
		String name = method.getName().substring(3);
		if (name.endsWith("AsString")) {
			name = name.substring(0, name.length() - "AsString".length());
		}
		Class<?> parameterType = method.getParameterTypes()[0];
		if (boolean.class.equals(parameterType)) {
			parameterType = Boolean.class;
		}
		else if (String[].class.equals(parameterType)) {
			parameterType = List.class;
		}
		return new Object[] { name, parameterType };
	}

	public FlywayPropertiesTests(String propertyName, Class<?> propertyType) {
		this.propertyName = propertyName;
		this.propertyType = propertyType;
	}

	@Test
	public void hasMatchingProperty() {
		assertThat(ReflectionUtils.findMethod(FlywayProperties.class,
				"set" + this.propertyName, this.propertyType)).isNotNull();
	}

	@Test
	public void propertyIsApplied() {
		FlywayProperties properties = new FlywayProperties();
		Object testValue = getTestValue();
		ReflectionUtils.invokeMethod(findSetter(), properties, testValue);
		Flyway flyway = new Flyway();
		properties.apply(flyway);
		Object appliedValue = convert(ReflectionUtils.invokeMethod(findGetter(), flyway));
		assertThat(appliedValue).isEqualTo(testValue);
	}

	private Object convert(Object appliedValue) {
		if (appliedValue instanceof String[]) {
			return Arrays.asList((String[]) appliedValue);
		}
		if (appliedValue instanceof MigrationVersion) {
			return ((MigrationVersion) appliedValue).getVersion();
		}
		return appliedValue;
	}

	private Method findSetter() {
		return ReflectionUtils.findMethod(FlywayProperties.class,
				"set" + this.propertyName, this.propertyType);
	}

	private Method findGetter() {
		return ReflectionUtils.findMethod(Flyway.class,
				(this.propertyType.equals(Boolean.class) ? "is" : "get")
						+ this.propertyName);
	}

	private Object getTestValue() {
		if (this.propertyType.equals(String.class)) {
			return "1.2.3";
		}
		if (this.propertyType.equals(Boolean.class)) {
			return new Random().nextBoolean();
		}
		if (this.propertyType.equals(List.class)) {
			return Arrays.asList("classpath:__one__", "classpath:__two__");
		}
		if (this.propertyType.equals(Map.class)) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("a", "alpha");
			return map;
		}
		return null;
	}

}
