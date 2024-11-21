package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiIdentifiable;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiArray;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiObject;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class for interpreting JSON:API values.
 * When serializing and deserializing JSON:API values, a JsonMapper must be provided. This is because we need a
 * JsonMapper with custom serializers/deserializers, not a static JsonMapper via JsonMapper.createDefault().
 * @see <a href="https://jsonapi.org/">JSON:API</a>
 */
public class JsonApiUtil {

	private JsonApiUtil() {}

	/**
	 * Read the ID from the JSON:API object's data and convert it to a long, but return null if it doesn't match the
	 * expected long value
	 * @param obj the JSON:API object
	 * @param expectedId the expected long value ID
	 * @return the ID of the JSON:API object's data as a long, or null if the ID in the JSON:API object's data does not
	 * match the expected long value ID
	 * @throws HttpStatusException if the JSON:API object's data is null
	 */
	public static Optional<Long> readAndValidateLongId(
			JsonApiObject<JsonApiResource> obj, long expectedId
	) throws HttpStatusException {
		return readLongId(obj)
				// Check that the object ID matches the expected ID
				.map(objId -> objId.longValue() == expectedId ? objId : null);
	}

	/**
	 * Read the ID from the JSON:API object's data and transform it to a long
	 * @param obj the JSON:API object
	 * @return the translated ID from the JSON:API object's data
	 * @throws HttpStatusException if the JSON:API object's data is null
	 */
	public static Optional<Long> readLongId(JsonApiObject<JsonApiResource> obj) throws HttpStatusException {
		return readId(obj, JsonApiUtil::parseLongNoThrow);
	}

	/**
	 * Read the ID from the JSON:API object's data and transform it to an object according to the provided translator
	 * @param obj the JSON:API object
	 * @param idTranslator the translator to convert the String ID to another type
	 * @return the translated ID from the JSON:API object's data
	 * @param <T> the type of ID to return
	 * @throws HttpStatusException if the JSON:API object's data is null
	 */
	public static <T> Optional<T> readId(
			JsonApiObject<JsonApiResource> obj, Function<String, Optional<T>> idTranslator
	) throws HttpStatusException {
		JsonApiResource data = obj.getData();
		if (data == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "data required");
		}
		return idTranslator.apply(data.getId());
	}

	/**
	 * Parse a string into a number, returning an empty "optional" if an exception occurs
	 */
	private static Optional<Long> parseLongNoThrow(String numStr) {
		try {
			return Optional.of(Long.valueOf(numStr));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	/**
	 * Transform the JSON:API object's data into the provided instance type, including the ID, throwing if the value is
	 * null
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param jsonApiObj the JSON:API object to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API object's data to
	 * @return the JSON:API object's data as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public static @NonNull <T extends JsonApiIdentifiable> T readDataWithId(
			JsonMapper jsonMapper, JsonApiObject<JsonApiResource> jsonApiObj, Class<T> clazz
	) throws HttpStatusException {
		JsonApiResource res = jsonApiObj.getData();
		return readResourceWithIdOrThrow(jsonMapper, res, clazz);
	}

	/**
	 * Transform the JSON:API object's data into a list of the provided instance types, including the IDs, throwing if
	 * any value is null
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param jsonApiObj the JSON:API object to convert to a list of class instances
	 * @param clazz the type of class to convert each item of the JSON:API object's data to
	 * @return the JSON:API object's data as a list of the specific instance type
	 * @param <T> the type of instances to return
	 * @throws HttpStatusException if any value is null
	 */
	public static @NonNull <T extends JsonApiIdentifiable> List<T> readDataWithIds(
			JsonMapper jsonMapper, JsonApiObject<JsonApiArray> jsonApiObj, Class<T> clazz
	) throws HttpStatusException {
		JsonApiArray arr = jsonApiObj.getData();
		return arr.stream()
				.map(res -> readResourceWithIdOrThrow(jsonMapper, res, clazz))
				.toList();
	}

	/**
	 * Transform the JSON:API object's data into the provided instance type, excluding the ID, throwing if the value is
	 * null
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param jsonApiObj the JSON:API object to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API object's data to
	 * @return the JSON:API object's data as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public static @NonNull <T> T readDataWithoutId(
			JsonMapper jsonMapper, JsonApiObject<JsonApiResource> jsonApiObj, Class<T> clazz
	) throws HttpStatusException {
		JsonApiResource res = jsonApiObj.getData();
		return readResourceWithoutId(jsonMapper, res, clazz)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Value cannot be null"));
	}

	/**
	 * Transform the JSON:API resource into the provided instance type, excluding the ID
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param res the JSON:API resource to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API resource to
	 * @return the JSON:API resource as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if unable to convert the resource into the provided instance type, such as if the
	 *             data is null
	 */
	public static <T> Optional<T> readResourceWithoutId(
			JsonMapper jsonMapper, JsonApiResource res, Class<T> clazz
	) throws HttpStatusException {
		if (res == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "data required");
			//} else if (res.getAttributes() == null) {
			//	throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Attributes are required");
		}

		Map<String, Object> properties = res.getAttributes();

		// Also get IDs from "relationships"
		if (res.getRelationships() != null) {
			Map<String, ?> relationshipIdMap = res.getRelationships().entrySet().stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							e -> {
								JsonApiDataType data = e.getValue().getData();
								return data.toResourceIds();
							}
					));
			properties = properties == null ? new HashMap<>() : new HashMap<>(properties);
			properties.putAll(relationshipIdMap);
		}

		return readNullableValue(jsonMapper, properties, clazz);
	}

	/**
	 * Transform the JSON:API resource into the provided instance type, including the ID
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param res the JSON:API resource to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API resource to
	 * @return the JSON:API resource as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if unable to convert the resource into the provided instance type, such as if the
	 *             data is null
	 */
	public static <T extends JsonApiIdentifiable> Optional<T> readResourceWithId(
			JsonMapper jsonMapper, JsonApiResource res, Class<T> clazz
	) throws HttpStatusException {
		return readResourceWithoutId(jsonMapper, res, clazz)
				.map(r -> {
					r.applyJsonApiId(res.getId());
					return r;
				});
	}

	/**
	 * Transform the JSON:API resource into the provided instance type, including the ID, throwing if the value is null
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param res the JSON:API resource to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API resource to
	 * @return the JSON:API resource as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public static @NonNull <T extends JsonApiIdentifiable> T readResourceWithIdOrThrow(
			JsonMapper jsonMapper, JsonApiResource res, Class<T> clazz
	) throws HttpStatusException {
		return readResourceWithId(jsonMapper, res, clazz)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Value cannot be null"));
	}

	/**
	 * Read a map or null value into an instance of the provided class
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param map the map to convert to a class instance
	 * @param clazz the type of class to convert the map to
	 * @return the non-null instance value
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public static @NonNull <T> T readValue(
			JsonMapper jsonMapper, Map<String, ?> map, Class<T> clazz
	) throws HttpStatusException {
		return readNullableValue(jsonMapper, map, clazz)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Value cannot be null"));
	}

	/**
	 * Read a map or null value into an instance of the provided class
	 * @param jsonMapper the JSON mapper - this must be provided in case there are custom serializers/deserializers
	 * @param map the map to convert to a class instance
	 * @param clazz the type of class to convert the map to
	 * @return the instance value as an Optional
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if unable to convert the map into the provided instance type
	 */
	public static <T> Optional<T> readNullableValue(
			JsonMapper jsonMapper, Map<String, ?> map, Class<T> clazz
	) throws HttpStatusException {
		try {
			String jsonStr = map == null ? null : jsonMapper.writeValueAsString(map);
			T val = jsonStr == null ? null : jsonMapper.readValue(jsonStr, clazz);
			return Optional.ofNullable(val);
		} catch (IOException e) {
			//log.info("Unable to read nullable value", e);
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected object format for nullable value");
		}
	}
}
