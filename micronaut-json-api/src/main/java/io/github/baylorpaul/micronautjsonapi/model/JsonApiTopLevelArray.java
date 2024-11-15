package io.github.baylorpaul.micronautjsonapi.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A top-level response specifically as an array, as defined by <a href="https://jsonapi.org/">JSON:API</a>.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class JsonApiTopLevelArray extends JsonApiTopLevelObject<JsonApiArray> {

	/**
	 * Change "setData" to be a list instead of a JsonApiArray. This ensures the deserializer assumes "data" is a list
	 * instead of an object (for JsonApiArray), due to use of generics. This avoids a SerdeException of "Error decoding
	 * property" due to "Unexpected token START_ARRAY, expected START_OBJECT".
	 * A better solution would be to use a <a href="https://micronaut-projects.github.io/micronaut-serialization/latest/guide/#serdes">custom deserializer</a> for JsonApiArray, and remove this setter.
	 * @param data the document's "primary data", if any
	 */
	public void setData(List<JsonApiResource> data) {
		super.setData(data == null ? null : new JsonApiArray(data));
	}
}
