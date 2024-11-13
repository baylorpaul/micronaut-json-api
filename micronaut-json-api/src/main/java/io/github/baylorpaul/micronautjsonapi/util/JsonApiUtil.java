package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiObject;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.util.Optional;
import java.util.function.Function;

public class JsonApiUtil {

	public static Optional<Long> readAndValidateLongId(
			JsonApiObject<JsonApiResource> obj, long expectedId
	) throws HttpStatusException {
		return readLongId(obj)
				// Check that the object ID matches the expected ID
				.map(objId -> objId.longValue() == expectedId ? objId : null);
	}

	public static Optional<Long> readLongId(JsonApiObject<JsonApiResource> obj) throws HttpStatusException {
		return readId(obj, JsonApiUtil::parseLongNoThrow);
	}

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
