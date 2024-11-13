package io.github.baylorpaul.micronautjsonapi.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

/** A <a href="https://jsonapi.org/">JSON:API</a> object resource. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Serdeable
@ReflectiveAccess
public class JsonApiResource extends JsonApiResourceIdentifier {
	/** an attributes object representing some of the resource's data. */
	private @Nullable SequencedMap<String, Object> attributes;
	/** relationships between the resource and other resources. */
	private @Nullable SequencedMap<String, JsonApiObject<? extends JsonApiDataType>> relationships;
	//private @Nullable JsonApiLinks links;

	@Builder
	public JsonApiResource(String type, String id, SequencedMap<String, Object> attributes, SequencedMap<String, JsonApiObject<? extends JsonApiDataType>> relationships) {
		super(type, id);
		this.attributes = attributes;
		this.relationships = relationships;
	}

	/**
	 * Add more attributes to the resource
	 * @param newAttributes attributes to add to the existing attributes. These attributes will replace the old
	 *            attributes when the keys match.
	 */
	public void putAllAttributes(SequencedMap<String, Object> newAttributes) {
		if (!CollectionUtils.isEmpty(newAttributes)) {
			if (CollectionUtils.isEmpty(attributes)) {
				this.attributes = newAttributes;
			} else {
				SequencedMap<String, Object> result = new LinkedHashMap<>(this.attributes);
				result.putAll(newAttributes);
				this.attributes = result;
			}
		}
	}
}
