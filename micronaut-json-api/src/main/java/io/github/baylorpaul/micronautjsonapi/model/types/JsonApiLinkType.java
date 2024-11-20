package io.github.baylorpaul.micronautjsonapi.model.types;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

/** A link type, which will either be a String or a link object, as defined by <a href="https://jsonapi.org/format/#document-links">JSON:API Links</a> */
@Serdeable
public interface JsonApiLinkType {
	/**
	 * Find the URI for the link
	 * @return the URI for the link
	 */
	@NonNull String toUri();
}
