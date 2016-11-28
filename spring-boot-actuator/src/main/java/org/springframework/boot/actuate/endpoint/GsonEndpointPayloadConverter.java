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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * An {@link EndpointPayloadConverter} that uses Gson.
 *
 * @author Andy Wilkinson
 */
public class GsonEndpointPayloadConverter implements EndpointPayloadConverter {

	private final TypeToken<List<Object>> objectList = new TypeToken<List<Object>>() {
	};

	private final TypeToken<Map<String, Object>> stringObjectMap = new TypeToken<Map<String, Object>>() {
	};

	private final Gson gson;

	public GsonEndpointPayloadConverter(Gson gson) {
		this.gson = gson;
	}

	@Override
	public List<Object> convertToObjectList(Object input) {
		return roundTrip(this.objectList.getType(), input);
	}

	@Override
	public Map<String, Object> convertToStringObjectMap(Object input) {
		return roundTrip(this.stringObjectMap.getType(), input);
	}

	private <T> T roundTrip(Type type, Object input) {
		return this.gson.fromJson(this.gson.toJson(input, type), type);
	}

}
