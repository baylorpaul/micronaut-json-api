package io.github.baylorpaul.micronautjsonapi.serialization;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiArray;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Since the "data" field on JsonApiObject is generic and can be either an object or an array of objects, provide a
 * deserializer for JsonApiDataType, which will appropriately set the "JsonApiObject.data" to either a JsonApiResource
 * or JsonApiArray. For example, the "relationships" in JsonApiResource may have JsonApiObject values, where some have
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
 */
@Singleton
public class JsonApiDataTypeDeserializer implements Deserializer<JsonApiDataType> {

	@Inject
	private JsonMapper jsonMapper;

	@Override
	public @Nullable JsonApiDataType deserialize(
			@NonNull Decoder decoder, @NonNull DecoderContext context,
			@NonNull Argument<? super JsonApiDataType> type
	) throws IOException {
		Object obj1 = decoder.decodeArbitrary();
		if (obj1 == null) {
			return null;
		} else if (obj1 instanceof Map<?, ?> dataMap) {
			return toJsonApiResource(dataMap);
		} else if (obj1 instanceof List<?> l1) {
			return toJsonApiArray(l1);
		} else {
			throw new IllegalArgumentException("Unexpected data type: " + obj1.getClass().getName());
		}
	}

	private JsonApiResource toJsonApiResource(Map<?, ?> map) throws IOException {
		return jsonMapper.readValue(toJson(map), JsonApiResource.class);
	}

	private JsonApiArray toJsonApiArray(List<?> list) throws IOException {
		List<JsonApiResource> l = new ArrayList<>(list.size());
		for (Object item : list) {
			if (item instanceof Map<?, ?> itemMap) {
				l.add(toJsonApiResource(itemMap));
			} else {
				throw new IllegalArgumentException("Unexpected list item type: " + item.getClass().getName());
			}
		}
		return new JsonApiArray(l);
	}

	/**
	 * Translate an object to JSON (JavaScript Object Notation)
	 * @param obj the object to translate, or null
	 * @return the object as a JSON string
	 * @throws IOException if an unrecoverable error occurs
	 */
	public String toJson(Object obj) throws IOException {
		return obj == null ? "null" : jsonMapper.writeValueAsString(obj);
	}
}
