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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

/**
 * @author awilkinson
 */
class FieldDescriptorLoader {

	@SuppressWarnings("unchecked")
	public List<FieldDescriptor> load(String resourceName) {
		Map<String, Map<String, Object>> yaml = (Map<String, Map<String, Object>>) new Yaml()
				.load(getClass().getResourceAsStream(resourceName));
		return yaml.entrySet().stream().map(this::createDescriptor)
				.collect(Collectors.toList());
	}

	private FieldDescriptor createDescriptor(
			Map.Entry<String, Map<String, Object>> entry) {
		FieldDescriptor descriptor = isSubsection(entry.getValue())
				? subsectionWithPath(entry.getKey()) : fieldWithPath(entry.getKey());
		descriptor.description(entry.getValue().get("description"));
		if (isOptional(entry.getValue())) {
			descriptor.optional();
		}
		return descriptor.type(getType(entry.getValue()));
	}

	private boolean isSubsection(Map<String, Object> descriptorConfig) {
		return getBoolean("subsection", descriptorConfig);
	}

	private boolean isOptional(Map<String, Object> descriptorConfig) {
		return getBoolean("optional", descriptorConfig);
	}

	private boolean getBoolean(String name, Map<String, Object> config) {
		Boolean value = (Boolean) config.get(name);
		return value != null && value;
	}

	private JsonFieldType getType(Map<String, Object> descriptorConfig) {
		String type = (String) descriptorConfig.get("type");
		return type == null ? null
				: JsonFieldType.valueOf(type.toUpperCase(Locale.ENGLISH));
	}

}
