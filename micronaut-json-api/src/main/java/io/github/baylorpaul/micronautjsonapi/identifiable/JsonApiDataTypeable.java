package io.github.baylorpaul.micronautjsonapi.identifiable;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;

/** An empty interface, allowing for a central type when "data.relationship" values may be a single entity or a list */
public interface JsonApiDataTypeable {

	/**
	 * Map the resource or resources to the corresponding type containing only the "type" and "id" for each
	 * @return a JSON:API resource identifier (type and id) or list of JSON:API resource identifiers
	 */
	JsonApiDataType toResourceIdOrIds();
}
