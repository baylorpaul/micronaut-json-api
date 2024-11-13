package io.github.baylorpaul.micronautjsonapi.model;

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

	@Builder(builderMethodName = "topLevelResourceBuilder")
	public JsonApiTopLevelResource(
			@Nullable SequencedMap<String, Object> meta,
			@Nullable JsonApiResource data,
			Collection<JsonApiError> errors,
			JsonApiArray included
	) {
		super(meta, data, errors, included);
	}
}
