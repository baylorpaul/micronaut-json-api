package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;

import java.util.*;

/** A <a href="https://jsonapi.org/">JSON:API</a> array of resources. */
@Serdeable
@ReflectiveAccess
public class JsonApiArray extends LinkedList<JsonApiResource> implements JsonApiDataType {
	public JsonApiArray() {}

	public JsonApiArray(List<? extends JsonApiResource> list) {
		super(list == null ? Collections.emptyList() : list);
	}

	public JsonApiArray(Collection<? extends JsonApiResourceable> list) {
		if (list != null) {
			list.stream().map(JsonApiResourceable::toResource).forEach(this::add);
		}
	}

	@Override
	public List<? extends SequencedMap<String, ?>> toResourceIds() {
		return this.stream()
				.map(JsonApiResourceIdentifier::toResourceIds)
				.toList();
	}
}
