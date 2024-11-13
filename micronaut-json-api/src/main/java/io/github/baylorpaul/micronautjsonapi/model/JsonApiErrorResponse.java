package io.github.baylorpaul.micronautjsonapi.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/** A <a href="https://jsonapi.org/">JSON:API</a> error response. */
@Data
@Builder
@Serdeable
@ReflectiveAccess
public class JsonApiErrorResponse {
	/**
	 * The error message, such as "Conflict" when it's an HTTP 409. "message" is not technically part of the standard
	 * JSON:API error response. I left this here to not exclude some data previously returned by
	 * HateoasErrorResponseProcessor.
	 */
	private String message;
	/**
	 * Errors while processing the API request
	 */
	@Singular
	private List<JsonApiError> errors;
}
