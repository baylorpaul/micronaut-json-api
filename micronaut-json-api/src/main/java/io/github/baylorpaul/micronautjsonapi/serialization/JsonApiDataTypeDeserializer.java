package io.github.baylorpaul.micronautjsonapi.serialization;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiDataType;
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
		Object obj = decoder.decodeArbitrary();
		return JsonApiSerdeUtil.deserializeJsonApiDataType(jsonMapper, obj);
	}
}
