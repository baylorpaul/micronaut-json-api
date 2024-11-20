package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.SequencedMap;

/**
 * A top-level response specifically as a resource, as defined by <a href="https://jsonapi.org/">JSON:API</a>.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class JsonApiTopLevelResource extends JsonApiTopLevelObject<JsonApiResource> {

	/**
	 * Create a top-level JSON:API resource
	 * @param links links related to the top-level object
	 * @param meta a meta object that contains non-standard meta-information, if any
	 * @param data the document's "primary data", if any
	 * @param errors error objects, if any
	 * @param included an array of resource objects that are related to the primary data and/or each other ("included resources")
	 */
	@Builder(builderMethodName = "topLevelResourceBuilder")
	public JsonApiTopLevelResource(
			@Nullable SequencedMap<String, JsonApiLinkType> links,
			@Nullable SequencedMap<String, Object> meta,
			@Nullable JsonApiResource data,
			Collection<JsonApiError> errors,
			JsonApiArray included
	) {
		super(links, meta, data, errors, included);
	}
}
