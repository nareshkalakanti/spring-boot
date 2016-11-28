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

/**
 * An {@link EndpointPayloadConverter} is used to convert the payloads returned by
 * endpoints such that they are then suitable for returning via JMX.
 *
 * @author Andy Wilkinson
 * @since 1.5.0
 */
public interface EndpointPayloadConverter {

	/**
	 * Converts the given input into a list
	 *
	 * @param input the input
	 * @return the converted list
	 */
	List<Object> convertToObjectList(Object input);

	/**
	 * Converts the given input into a map
	 * @param input the input
	 * @return the converted map
	 */
	Map<String, Object> convertToStringObjectMap(Object input);

}
