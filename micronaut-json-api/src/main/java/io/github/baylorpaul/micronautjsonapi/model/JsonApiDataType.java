package io.github.baylorpaul.micronautjsonapi.model;

import io.micronaut.serde.annotation.Serdeable;

/** An empty interface, implementing by acceptable data types defined by <a href="https://jsonapi.org/">JSON:API</a> */
@Serdeable
public interface JsonApiDataType {
	/**
	 * Either a map with an "id" key or a list of maps each containing an "id" key. This does not necessarily need to
	 * contain the "type" as well because it is used for known entities. I.e. this is for mapping from JSON:API to
	 * entities, not the other way around. E.g. in an entity with member "User author", for "author.id", we already know
	 * that "author" is of type "User".
	 * @return either a map with an "id" key, or a list of maps, each with an "id" key
	 */
	Object toResourceIds();
}
