package io.github.baylorpaul.micronautjsonapi.serialization;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiLinkString;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serializer;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * Serialize the JsonApiLinkString as a String.
 * We don't need a deserializer because this is handled by JsonApiLinkTypeDeserializer.
 */
@Singleton
public class JsonApiLinkStringSerializer implements Serializer<JsonApiLinkString> {

	@Override
	public void serialize(
			@NonNull Encoder encoder, @NonNull EncoderContext context,
			@NonNull Argument<? extends JsonApiLinkString> type, @NonNull JsonApiLinkString value
	) throws IOException {
		encoder.encodeString(value.getUri());
	}
}
