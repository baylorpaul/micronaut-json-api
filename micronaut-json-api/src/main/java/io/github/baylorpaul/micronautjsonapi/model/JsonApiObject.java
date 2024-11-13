package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.serialization.JsonApiDataTypeDeserializer;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.SequencedMap;

/**
 * A <a href="https://jsonapi.org/">JSON:API</a> object.
 * E.g.
 * <pre>
 * {
 *   data: {
 *     type: "grantingToken",
 *     id: 555,
 *     attributes: {
 *       purpose: "APP_REFRESH_TOKEN",
 *       comment: "This token is for XYZ",
 *     },
 *     relationships: {
 *       user: {
 *         data: {
 *           type: "user",
 *           id: 777,
 *           attributes: {
 *             email: "john@example.com",
 *             name: "John Doe",
 *             enabled: true,
 *           },
 *         },
 *       },
 *     },
 *   }
 * }
 * </pre>
 */
@Data
@Builder
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
@ReflectiveAccess
public class JsonApiObject<T extends JsonApiDataType> {
	/** a meta object that contains non-standard meta-information. */
	private @Nullable SequencedMap<String, Object> meta;

	/**
	 * a single resource, a single resource identifier, an array of resources, an array of resource identifiers, or
	 * null. A logical collection of resources MUST be represented as a non-null array, even if it only contains one
	 * item or is empty.
	 */
	@Serdeable.Deserializable(using = JsonApiDataTypeDeserializer.class)
	private @Nullable T data;
}
