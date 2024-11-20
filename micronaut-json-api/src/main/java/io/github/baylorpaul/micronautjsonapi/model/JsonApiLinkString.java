package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.github.baylorpaul.micronautjsonapi.serialization.JsonApiLinkStringSerializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A <a href="https://jsonapi.org/format/#document-links">JSON:API Link string</a>. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
@Serdeable.Serializable(using = JsonApiLinkStringSerializer.class)
@ReflectiveAccess
public class JsonApiLinkString implements JsonApiLinkType {

	/** the URI for the link */
	private @NonNull String uri;

	@Override
	public String toUri() {
		return uri;
	}
}
