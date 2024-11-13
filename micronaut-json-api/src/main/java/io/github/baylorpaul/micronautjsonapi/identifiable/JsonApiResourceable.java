package io.github.baylorpaul.micronautjsonapi.identifiable;

import io.github.baylorpaul.micronautjsonapi.model.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public interface JsonApiResourceable extends JsonApiIdentifiable {

	String toResourceType();
	SequencedMap<String, Object> toJsonApiAttributes();

	/** Find the relationship entities, in a map with consistently ordered keys if there are multiple entries */
	default SequencedMap<String, ? extends JsonApiDataTypeable> toRelationships() {
		return null;
	}

	default SequencedMap<String, JsonApiObject<? extends JsonApiDataType>> toJsonApiRelationships() {
		SequencedMap<String, ? extends JsonApiDataTypeable> relationships = toRelationships();
		LinkedHashMap<String, JsonApiObject<? extends JsonApiDataType>> result = null;
		if (relationships != null) {
			result = new LinkedHashMap<>();
			for (Map.Entry<String, ? extends JsonApiDataTypeable> e : relationships.entrySet()) {
				result.put(e.getKey(), toJsonApiObject(e.getValue()));
			}
		}
		return result;
	}

	private static JsonApiObject<? extends JsonApiDataType> toJsonApiObject(JsonApiDataTypeable entity) {
		return new JsonApiObject<>(
				null,
				entity == null ? null : entity.toResourceIdOrIds()
		);
	}

	/**
	 * Map to a JsonApiResource with only its "type" and "id"
	 */
	@Override
	default JsonApiResource toResourceIdOrIds() {
		return JsonApiResource.builder()
				.type(toResourceType())
				.id(toJsonApiId())
				.build();
	}

	/**
	 * Build the resource, providing relationship IDs, but not the relationship attributes
	 */
	default JsonApiResource toResource() {
		return JsonApiResource.builder()
				.type(toResourceType())
				.id(toJsonApiId())
				.attributes(toJsonApiAttributes())
				.relationships(toJsonApiRelationships())
				.build();
	}

	default JsonApiTopLevelResource toTopLevelResource() {
		return JsonApiTopLevelResource.topLevelResourceBuilder()
				.data(toResource())
				.build();
	}
}
