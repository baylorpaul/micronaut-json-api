package io.github.baylorpaul.micronautjsonapi.service;

import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiIdentifiable;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiArray;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiObject;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A service to serialize and deserialize JSON:API values. This is a service instead of a utility class because we need
 * a JsonMapper with custom serializers/deserializers, not a static JsonMapper.
 * @see <a href="https://jsonapi.org/">JSON:API</a>
 */
@Singleton
public class JsonApiService {

	/**
	 * The JsonMapper. We're not just using a static value of JsonMapper.createDefault() because we need access to
	 * custom serializers and deserializers.
	 */
	@Inject
	private JsonMapper jsonMapper;

	/**
	 * Transform the JSON:API object's data into the provided instance type, including the ID, throwing if the value is
	 * null
	 * @param jsonApiObj the JSON:API object to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API object's data to
	 * @return the JSON:API object's data as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public @NonNull <T extends JsonApiIdentifiable> T readDataWithId(
			JsonApiObject<JsonApiResource> jsonApiObj, Class<T> clazz
	) throws HttpStatusException {
		JsonApiResource res = jsonApiObj.getData();
		return readResourceWithIdOrThrow(res, clazz);
	}

	/**
	 * Transform the JSON:API object's data into a list of the provided instance types, including the IDs, throwing if
	 * any value is null
	 * @param jsonApiObj the JSON:API object to convert to a list of class instances
	 * @param clazz the type of class to convert each item of the JSON:API object's data to
	 * @return the JSON:API object's data as a list of the specific instance type
	 * @param <T> the type of instances to return
	 * @throws HttpStatusException if any value is null
	 */
	public @NonNull <T extends JsonApiIdentifiable> List<T> readDataWithIds(
			JsonApiObject<JsonApiArray> jsonApiObj, Class<T> clazz
	) throws HttpStatusException {
		JsonApiArray arr = jsonApiObj.getData();
		return arr.stream()
				.map(res -> readResourceWithIdOrThrow(res, clazz))
				.toList();
	}

	/**
	 * Transform the JSON:API object's data into the provided instance type, excluding the ID, throwing if the value is
	 * null
	 * @param jsonApiObj the JSON:API object to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API object's data to
	 * @return the JSON:API object's data as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public @NonNull <T> T readDataWithoutId(
			JsonApiObject<JsonApiResource> jsonApiObj, Class<T> clazz
	) throws HttpStatusException {
		JsonApiResource res = jsonApiObj.getData();
		return readResourceWithoutId(res, clazz)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Value cannot be null"));
	}

	/**
	 * Transform the JSON:API resource into the provided instance type, excluding the ID
	 * @param res the JSON:API resource to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API resource to
	 * @return the JSON:API resource as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if unable to convert the resource into the provided instance type, such as if the
	 *             data is null
	 */
	public <T> Optional<T> readResourceWithoutId(
			JsonApiResource res, Class<T> clazz
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

		return readNullableValue(properties, clazz);
	}

	/**
	 * Transform the JSON:API resource into the provided instance type, including the ID
	 * @param res the JSON:API resource to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API resource to
	 * @return the JSON:API resource as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if unable to convert the resource into the provided instance type, such as if the
	 *             data is null
	 */
	public <T extends JsonApiIdentifiable> Optional<T> readResourceWithId(
			JsonApiResource res, Class<T> clazz
	) throws HttpStatusException {
		return readResourceWithoutId(res, clazz)
				.map(r -> {
					r.applyJsonApiId(res.getId());
					return r;
				});
	}

	/**
	 * Transform the JSON:API resource into the provided instance type, including the ID, throwing if the value is null
	 * @param res the JSON:API resource to convert to a class instance
	 * @param clazz the type of class to convert the JSON:API resource to
	 * @return the JSON:API resource as a specific instance type
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public @NonNull <T extends JsonApiIdentifiable> T readResourceWithIdOrThrow(
			JsonApiResource res, Class<T> clazz
	) throws HttpStatusException {
		return readResourceWithId(res, clazz)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Value cannot be null"));
	}

	/**
	 * Read a map or null value into an instance of the provided class
	 * @param map the map to convert to a class instance
	 * @param clazz the type of class to convert the map to
	 * @return the non-null instance value
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if the value is null
	 */
	public @NonNull <T> T readValue(Map<String, ?> map, Class<T> clazz) throws HttpStatusException {
		return readNullableValue(map, clazz)
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "Value cannot be null"));
	}

	/**
	 * Read a map or null value into an instance of the provided class
	 * @param map the map to convert to a class instance
	 * @param clazz the type of class to convert the map to
	 * @return the instance value as an Optional
	 * @param <T> the type of instance to return
	 * @throws HttpStatusException if unable to convert the map into the provided instance type
	 */
	public <T> Optional<T> readNullableValue(Map<String, ?> map, Class<T> clazz) throws HttpStatusException {
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
