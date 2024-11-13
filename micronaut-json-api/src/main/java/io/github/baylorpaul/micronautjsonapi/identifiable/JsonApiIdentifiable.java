package io.github.baylorpaul.micronautjsonapi.identifiable;

public interface JsonApiIdentifiable extends JsonApiDataTypeable {
	String toJsonApiId();
	void applyJsonApiId(String jsonApiId);
}
