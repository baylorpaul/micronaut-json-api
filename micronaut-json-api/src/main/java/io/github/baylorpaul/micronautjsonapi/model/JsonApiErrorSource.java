package io.github.baylorpaul.micronautjsonapi.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** A <a href="https://jsonapi.org/">JSON:API</a> error source. */
@Data
@SuperBuilder
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class JsonApiErrorSource {
	/**
	 * a JSON Pointer [RFC6901] to the value in the request document that caused the error [e.g. "/data" for a primary
	 * data object, or "/data/attributes/title" for a specific attribute]. This MUST point to a value in the request
	 * document that exists; if it doesn't, the client SHOULD simply ignore the pointer.
	 */
	private @Nullable String pointer;
	/** a string indicating which URI query parameter caused the error. */
	private @Nullable String parameter;
	/** a string indicating the name of a single request header which caused the error. */
	private @Nullable String header;
}
