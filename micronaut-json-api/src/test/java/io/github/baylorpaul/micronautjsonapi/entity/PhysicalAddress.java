package io.github.baylorpaul.micronautjsonapi.entity;

import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.SequencedMap;

/**
 * A physical address location such as a mailing address or billing address.
 */
@MappedEntity
@Data
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor
@ReflectiveAccess
public class PhysicalAddress implements JsonApiResourceable {

	private @Id @GeneratedValue @NonNull long id;
	private @Nullable String line1;
	private @Nullable String line2;
	private @Nullable String locality;
	private @Nullable String region;
	private @Nullable String postalCode;
	private @Nullable String country;
	private @GeneratedValue Instant created;
	private @GeneratedValue Instant updated;

	@Override
	public String toResourceType() {
		return "physicalAddress";
	}

	@Override
	public String toJsonApiId() {
		return Long.toString(id);
	}

	@Override
	public void applyJsonApiId(String jsonApiId) {
		setId(jsonApiId == null ? 0L : Long.parseLong(jsonApiId));
	}

	@Override
	public SequencedMap<String, Object> toJsonApiAttributes() {
		SequencedMap<String, Object> attrs = new LinkedHashMap<>();
		attrs.put("line1", line1);
		attrs.put("line2", line2);
		attrs.put("locality", locality);
		attrs.put("region", region);
		attrs.put("postalCode", postalCode);
		attrs.put("country", country);
		attrs.put("created", created);
		attrs.put("updated", updated);
		return attrs;
	}
}
