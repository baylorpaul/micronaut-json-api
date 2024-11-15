package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiObject;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.util.Optional;
import java.util.function.Function;

/**
 * A utility class for interpreting JSON:API values
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
}
