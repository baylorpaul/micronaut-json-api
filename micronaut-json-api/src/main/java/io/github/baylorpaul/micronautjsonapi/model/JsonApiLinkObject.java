package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.SequencedMap;

/** A <a href="https://jsonapi.org/format/#document-links">JSON:API Link object</a>. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
@ReflectiveAccess
public class JsonApiLinkObject implements JsonApiLinkType {

	/** a string whose value is a URI-reference [RFC3986 Section 4.1] pointing to the link's target. */
	private @NonNull String href;
	/** a string indicating the link's relation type. The string MUST be a valid link relation type. */
	private @Nullable String rel;
	/** a link to a description document (e.g. OpenAPI or JSON Schema) for the link target. */
	private @Nullable String describedby;
	/**
	 * a string which serves as a label for the destination of a link such that it can be used as a human-readable
	 * identifier (e.g., a menu entry).
	 */
	private @Nullable String title;
	/** a string indicating the media type of the link's target. */
	private @Nullable String type;
//	/**
//	 * a string or an array of strings indicating the language(s) of the link's target. An array of strings indicates
//	 * that the link's target is available in multiple languages. Each string MUST be a valid language tag [RFC5646].
//	 */
//	private @Nullable String|List<String> hreflang;
	/** a meta object containing non-standard meta-information about the link. */
	private @Nullable SequencedMap<String, Object> meta;

	@Override
	public String toUri() {
		return href;
	}
}
