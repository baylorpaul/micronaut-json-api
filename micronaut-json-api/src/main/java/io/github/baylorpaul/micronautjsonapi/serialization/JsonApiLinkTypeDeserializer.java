package io.github.baylorpaul.micronautjsonapi.serialization;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiLinkObject;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiLinkString;
import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.Map;

/**
 * Since the "links" field on JsonApiResource is generic and can be either a string or an object, provide a deserializer
 * that will map each entry to a JsonApiLinkType to a JsonApiLinkString, JsonApiLinkObject, or null.
 */
@Singleton
public class JsonApiLinkTypeDeserializer implements Deserializer<JsonApiLinkType> {

	@Inject
	private JsonMapper jsonMapper;

	@Override
	public @Nullable JsonApiLinkType deserialize(
			@NonNull Decoder decoder, @NonNull DecoderContext context,
			@NonNull Argument<? super JsonApiLinkType> type
	) throws IOException {
		Object obj1 = decoder.decodeArbitrary();
		if (obj1 == null) {
			return null;
		} else if (obj1 instanceof Map<?, ?> linkObj) {
			return jsonMapper.readValue(jsonMapper.writeValueAsString(linkObj), JsonApiLinkObject.class);
		} else if (obj1 instanceof String ls) {
			return new JsonApiLinkString(ls);
		} else {
			throw new IllegalArgumentException("Unexpected data type: " + obj1.getClass().getName());
		}
	}
}
