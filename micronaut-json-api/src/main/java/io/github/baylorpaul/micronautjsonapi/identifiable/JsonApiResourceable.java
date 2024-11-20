package io.github.baylorpaul.micronautjsonapi.identifiable;

import io.github.baylorpaul.micronautjsonapi.model.*;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

/**
 * An entity that may be transformed into a JSON:API resource
 */
public interface JsonApiResourceable extends JsonApiIdentifiable {

	/**
	 * Provide a type to uniquely identify resource type for the JSON:API object, such as "user", "article",
	 * "physicalAddress", etc.
	 * @return the uniquely identify resource type for the JSON:API object
	 */
	String toResourceType();

	/**
	 * Provide a map of attributes that describe the resources, with keys such as "email", "name", etc.
	 * @return a map of attributes that describe the resources
	 */
	SequencedMap<String, Object> toJsonApiAttributes();

	/**
	 * Find the relationship entities, in a map with consistently ordered keys if there are multiple entries
	 * @return the relationship entities in a map
	 */
	default SequencedMap<String, ? extends JsonApiDataTypeable> toRelationships() {
		return null;
	}

	/**
	 * Provide a map of relationships as JSON:API objects
	 * @return the relationships as JSON:API objects, or null if none
	 */
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

	/**
	 * Translate the entity to a JSON:API object
	 */
	private static JsonApiObject<? extends JsonApiDataType> toJsonApiObject(JsonApiDataTypeable entity) {
		return new JsonApiObject<>(
				null,
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
	 * @return the instance as a JSON:API resource
	 */
	default JsonApiResource toResource() {
		return JsonApiResource.builder()
				.type(toResourceType())
				.id(toJsonApiId())
				.attributes(toJsonApiAttributes())
				.relationships(toJsonApiRelationships())
				.build();
	}

	/**
	 * Convert the instance to a JSON:API top level resource
	 * @return the instance as a JSON:API top level resource
	 */
	default JsonApiTopLevelResource toTopLevelResource() {
		return JsonApiTopLevelResource.topLevelResourceBuilder()
				.data(toResource())
				.build();
	}
}
