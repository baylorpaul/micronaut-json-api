package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.SequencedMap;

/**
 * A top-level response, as defined by <a href="https://jsonapi.org/">JSON:API</a>.
 * @param <T> - The generic type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class JsonApiTopLevelObject<T extends JsonApiDataType> extends JsonApiObject<T> {
	private @Nullable Collection<JsonApiError> errors;
	/** an array of resource objects that are related to the primary data and/or each other ("included resources"). */
	private @Nullable JsonApiArray included;

	/**
	 * Create a top-level JSON:API object
	 * @param meta a meta object that contains non-standard meta-information, if any
	 * @param data the document's "primary data", if any
	 * @param errors error objects, if any
	 * @param included an array of resource objects that are related to the primary data and/or each other ("included resources")
	 */
	@Builder(builderMethodName = "topLevelBuilder")
	public JsonApiTopLevelObject(
			@Nullable SequencedMap<String, Object> meta,
			@Nullable T data,
			Collection<JsonApiError> errors,
			JsonApiArray included
	) {
		super(meta, data);
		this.errors = errors;
		this.included = included;
	}

	/**
	 * Change "setIncluded" to be a list instead of a JsonApiArray. This ensures the deserializer assumes "included" is
	 * a list instead of an object (for JsonApiArray), due to use of generics. This avoids a SerdeException of "Error
	 * decoding property" due to "Unexpected token START_ARRAY, expected START_OBJECT".
	 * A better solution would be to use a <a href="https://micronaut-projects.github.io/micronaut-serialization/latest/guide/#serdes">custom deserializer</a> for JsonApiArray, and remove this setter.
	 * @param included an array of resource objects that are related to the primary data and/or each other ("included resources")
	 */
	public void setIncluded(List<JsonApiResource> included) {
		this.included = included == null ? null : new JsonApiArray(included);
	}
}
