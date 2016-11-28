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

package org.springframework.boot.actuate.endpoint;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@EndpointPayloadConverter} that uses a Jackson {@link ObjectMapper}.
 *
 * @author Andy Wilkinson
 */
public class JacksonEndpointPayloadConverter implements EndpointPayloadConverter {

	private final JavaType objectList;

	private final JavaType stringObjectMap;

	private final ObjectMapper objectMapper;

	public JacksonEndpointPayloadConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.objectList = objectMapper.getTypeFactory()
				.constructParametricType(List.class, Object.class);
		this.stringObjectMap = objectMapper.getTypeFactory()
				.constructParametricType(Map.class, String.class, Object.class);
	}

	@Override
	public List<Object> convertToObjectList(Object input) {
		return this.objectMapper.convertValue(input, this.objectList);
	}

	@Override
	public Map<String, Object> convertToStringObjectMap(Object input) {
		return this.objectMapper.convertValue(input, this.stringObjectMap);
	}

}
