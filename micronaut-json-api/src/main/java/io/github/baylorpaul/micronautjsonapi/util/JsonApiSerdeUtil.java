package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiArray;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiLinkObject;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiLinkString;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.micronaut.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class for custom serializing/deserializing JSON:API values for ambiguous types.
 * E.g. when a "data" attribute may be a resource or an array of resources. Or when a link may be a string or an object.
 * @see <a href="https://jsonapi.org/">JSON:API</a>
 */
public class JsonApiSerdeUtil {

	private JsonApiSerdeUtil() {}

	/**
	 * Convert the JsonApiLinkString to a JSON value. This should just be represented as a string URI.
	 * @param jsonApiLinkString the object containing the string URI
	 * @return the JSON string representation of the JsonApiLinkString
	 */
	public static String serializeJsonApiLinkString(JsonApiLinkString jsonApiLinkString) {
		return jsonApiLinkString.getUri();
	}

	/**
	 * Map an object to a JsonApiDataType. This requires custom deserialization because it may be a resource or an array
	 * of resources. For example, the "relationships" in JsonApiResource may have JsonApiObject values, where some have
	 * "data" arrays, and some have "data" objects.
	 * E.g.
	 * <pre>
	 *   {
	 *     "relationships": {
	 *       "author": { "data": {"type": "people", "id": "9"} },
	 *       "comments": { "data": [ {"type": "comments", "id": "5"}, {"type": "comments", "id": "12"} ] }
	 *     }
	 *   }
	 * </pre>
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param obj the JSON:API resource or array of resources
	 * @return the object mapped to JsonApiResource or JsonApiArray
	 * @throws IOException - If an unrecoverable error occurs
	 */
	public static JsonApiDataType deserializeJsonApiDataType(JsonMapper jsonMapper, Object obj) throws IOException {
		if (obj == null) {
			return null;
		} else if (obj instanceof Map<?, ?> dataMap) {
			return toJsonApiResource(jsonMapper, dataMap);
		} else if (obj instanceof List<?> l1) {
			return toJsonApiArray(jsonMapper, l1);
		} else {
			throw new IllegalArgumentException("Unexpected data type: " + obj.getClass().getName());
		}
	}

	/**
	 * Map an object to a JsonApiLinkType. Because the "links" field on JsonApiResource is generic and can be either a
	 * string or an object, this will deserialize each entry to a JsonApiLinkString, JsonApiLinkObject, or null.
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param obj the JSON:API link string or link object
	 * @return the object mapped to JsonApiLinkString, JsonApiLinkObject, or null
	 * @throws IOException - If an unrecoverable error occurs
	 */
	public static JsonApiLinkType deserializeJsonApiLinkType(JsonMapper jsonMapper, Object obj) throws IOException {
		if (obj == null) {
			return null;
		} else if (obj instanceof Map<?, ?> linkObj) {
			return jsonMapper.readValue(jsonMapper.writeValueAsString(linkObj), JsonApiLinkObject.class);
		} else if (obj instanceof String ls) {
			return new JsonApiLinkString(ls);
		} else {
			throw new IllegalArgumentException("Unexpected data type: " + obj.getClass().getName());
		}
	}

	private static JsonApiResource toJsonApiResource(JsonMapper jsonMapper, Map<?, ?> map) throws IOException {
		return jsonMapper.readValue(toJson(jsonMapper, map), JsonApiResource.class);
	}

	private static JsonApiArray toJsonApiArray(JsonMapper jsonMapper, List<?> list) throws IOException {
		List<JsonApiResource> l = new ArrayList<>(list.size());
		for (Object item : list) {
			if (item instanceof Map<?, ?> itemMap) {
				l.add(toJsonApiResource(jsonMapper, itemMap));
			} else {
				throw new IllegalArgumentException("Unexpected list item type: " + item.getClass().getName());
			}
		}
		return new JsonApiArray(l);
	}

	/**
	 * Translate an object to JSON (JavaScript Object Notation)
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param obj the object to translate, or null
	 * @return the object as a JSON string
	 * @throws IOException if an unrecoverable error occurs
	 */
	private static String toJson(JsonMapper jsonMapper, Object obj) throws IOException {
		return obj == null ? "null" : jsonMapper.writeValueAsString(obj);
	}
}
