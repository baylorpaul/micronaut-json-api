package io.github.baylorpaul.micronautjsonapi.identifiable;

import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.data.annotation.Id;

/**
 * An entity with an ID that may be transformed into a JSON:API resource ID
 */
public interface JsonApiIdentifiable extends JsonApiDataTypeable {
	/**
	 * Provide the entity ID as a JSON:API String ID
	 * @return the entity ID as a JSON:API String ID
	 */
	default String toJsonApiId() {
		BeanProperty<JsonApiIdentifiable, Object> idProperty = findIdProperty();
		String id = null;
		if (idProperty != null) {
			Object value = idProperty.get(this);
			if (value != null) {
				id = value.toString();
			}
		}
		return id;
	}

	/**
	 * Set the ID using a JSON:API String ID. JSON:API requires that IDs be strings, but the supporting entity may use
	 * another type, such as Number.
	 * @param jsonApiId the JSON:API String ID
	 */
	default void applyJsonApiId(String jsonApiId) {
		BeanProperty<JsonApiIdentifiable, Object> idProperty = findIdProperty();
		if (jsonApiId == null) {
			idProperty.set(this, null);
		} else {
			if (idProperty != null) {
				Class<?> type = idProperty.getType();
				if (String.class.isAssignableFrom(type)) {
					idProperty.set(this, jsonApiId);
				} else if (long.class.isAssignableFrom(type)) {
					idProperty.set(this, Long.parseLong(jsonApiId));
				} else if (Long.class.isAssignableFrom(type)) {
					idProperty.set(this, Long.valueOf(jsonApiId));
				} else if (int.class.isAssignableFrom(type)) {
					idProperty.set(this, Integer.parseInt(jsonApiId));
				} else if (Integer.class.isAssignableFrom(type)) {
					idProperty.set(this, Integer.valueOf(jsonApiId));
				}
			}
		}
	}

	private BeanProperty<JsonApiIdentifiable, Object> findIdProperty() {
		final BeanWrapper<JsonApiIdentifiable> wrapper = BeanWrapper.getWrapper(this);
		BeanProperty<JsonApiIdentifiable, Object> idProperty = null;
		for (BeanProperty<JsonApiIdentifiable, Object> bp : wrapper.getBeanProperties()) {
			boolean isId = bp.getDeclaredAnnotation(Id.class) != null;
			if (isId) {
				idProperty = bp;
				break;
			}
		}
		return idProperty;
	}
}
