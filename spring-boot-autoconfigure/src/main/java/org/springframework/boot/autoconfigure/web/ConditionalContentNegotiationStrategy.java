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

package org.springframework.boot.autoconfigure.web;

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;

/**
 * A {@link ContentNegotiationStrategy} decorator that makes a strategy conditional on the
 * evaluation of a {@link ContentNegotiationCondition}.
 *
 * @author Andy Wilkinson
 * @since 1.5.3
 */
public class ConditionalContentNegotiationStrategy implements ContentNegotiationStrategy {

	/**
	 * The name of the request attribute used to locate the
	 * {@link ContentNegotiationCondition}.
	 */
	public static final String REQUEST_ATTRIBUTE_CONTENT_NEGOTIATION_CONDITION = ConditionalContentNegotiationStrategy.class
			.getName() + ".condition";

	private final ContentNegotiationStrategy delegate;

	/**
	 * Creates a new {@code ConditionalContentNegotiationStrategy} that will call the
	 * given {@code delegate} when there is no condition or the condition matches.
	 *
	 * @param delegate the delegate
	 */
	public ConditionalContentNegotiationStrategy(ContentNegotiationStrategy delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest)
			throws HttpMediaTypeNotAcceptableException {
		ContentNegotiationCondition condition = (ContentNegotiationCondition) webRequest
				.getAttribute(REQUEST_ATTRIBUTE_CONTENT_NEGOTIATION_CONDITION,
						RequestAttributes.SCOPE_REQUEST);
		if (condition == null || condition.matches(this.delegate)) {
			return this.delegate.resolveMediaTypes(webRequest);
		}
		return Collections.emptyList();
	}

	/**
	 * A condition that determines whether a {@link ContentNegotiationStrategy} should be
	 * called.
	 *
	 * @since 1.5.3
	 */
	public interface ContentNegotiationCondition {

		/**
		 * Returns whether or not the given {@code strategy} should be involved in content
		 * negotiation.
		 * @param strategy the strategy
		 * @return {@code true} to involve the strategy, otherwise {@code false}.
		 */
		boolean matches(ContentNegotiationStrategy strategy);

	}

}
