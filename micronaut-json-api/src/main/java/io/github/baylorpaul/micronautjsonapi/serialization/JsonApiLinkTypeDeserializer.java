package io.github.baylorpaul.micronautjsonapi.serialization;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.github.baylorpaul.micronautjsonapi.util.JsonApiSerdeUtil;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * Since the "links" field on JsonApiResource is generic and can be either a string or an object, provide a deserializer
 * that will map each entry to a JsonApiLinkString, JsonApiLinkObject, or null.
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
		Object obj = decoder.decodeArbitrary();
		return JsonApiSerdeUtil.deserializeJsonApiLinkType(jsonMapper, obj);
	}
}
