package io.github.baylorpaul.micronautjsonapi.identifiable;

/**
 * An entity with an ID that may be transformed into a JSON:API resource ID
 */
public interface JsonApiIdentifiable extends JsonApiDataTypeable {
	/**
	 * Provide the entity ID as a JSON:API String ID
	 * @return the entity ID as a JSON:API String ID
	 */
	String toJsonApiId();

	/**
	 * Set the ID using a JSON:API String ID. JSON:API requires that IDs be strings, but the supporting entity may use
	 * another type, such as Number.
	 * @param jsonApiId the JSON:API String ID
	 */
	void applyJsonApiId(String jsonApiId);
}
