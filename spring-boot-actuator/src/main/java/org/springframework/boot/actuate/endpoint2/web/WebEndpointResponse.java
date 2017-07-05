package org.springframework.boot.actuate.endpoint2.web;

/**
 * A dedicated response that {@link WebEndpoint} operations can return to provide
 * more context.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public final class WebEndpointResponse<T> {

	private final T body;

	private final int status;

	public WebEndpointResponse(T body, Integer status) {
		this.body = body;
		this.status = (status != null ? status : 200);
	}

	public WebEndpointResponse(T body) {
		this(body, null);
	}

	public T getBody() {
		return this.body;
	}

	public int getStatus() {
		return this.status;
	}

}
