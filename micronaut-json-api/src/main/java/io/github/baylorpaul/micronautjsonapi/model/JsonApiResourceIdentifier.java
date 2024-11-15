package io.github.baylorpaul.micronautjsonapi.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.SequencedMap;
import java.util.TreeMap;

/** A <a href="https://jsonapi.org/">JSON:API</a> object that identifies an individual resource. */
@Data
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class JsonApiResourceIdentifier implements JsonApiDataType {
	/** the resource type. Every resource object MUST contain a type member. */
	private String type;
	/**
	 * the ID of the resource. This must be a string. Every resource object MUST contain an id member, except when the
	 * resource object originates at the client and represents a new resource to be created on the server.
	 */
	private @Nullable String id;

	/**
	 * Create a top-level JSON:API resource identifier
	 * @param type the resource type. Every resource object MUST contain a type member.
	 * @param id the ID of the resource. This must be a string. Every resource object MUST contain an id member, except
	 *              when the resource object originates at the client and represents a new resource to be created on the
	 *              server.
	 */
	@Builder(builderMethodName = "idBuilder")
	public JsonApiResourceIdentifier(String type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public SequencedMap<String, ?> toResourceIds() {
		// This doesn't also need to provide "type" because we're using this to map from JSON:API to an entity, not the
		// other way around. E.g. in an entity with member "User author", for "author.id", we already know that "author"
		// is of type "User".
		return new TreeMap<>(Map.of("id", this.getId()));
	}
}
