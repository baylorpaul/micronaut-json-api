package io.github.baylorpaul.micronautjsonapi.identifiable;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiArray;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JsonApiArrayable extends LinkedList<JsonApiResourceable> implements JsonApiDataTypeable {
	public JsonApiArrayable() {}

	public JsonApiArrayable(List<? extends JsonApiResourceable> list) {
		super(list == null ? Collections.emptyList() : list);
	}

	/**
	 * Map to a JsonApiArray with each JsonApiResource containing only its "type" and "id"
	 */
	@Override
	public JsonApiArray toResourceIdOrIds() {
		// We're not using new JsonApiArray(this) because we only want the JsonApiResourceIdentifier attributes
		// to be available, not everything from JsonApiResource as well.
		return new JsonApiArray(this.stream().map(JsonApiResourceable::toResourceIdOrIds).toList());
	}
}
