package io.github.baylorpaul.micronautjsonapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.SequencedMap;

/**
 * A user of the platform
 */
@MappedEntity
@Data
@Builder(toBuilder = true)
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor
@ReflectiveAccess
public class User implements JsonApiResourceable {

	private @Id @GeneratedValue @NonNull long id;
	private @NotBlank String email;
	private @NotBlank String name;
	private @JsonIgnore @Nullable String password;
	private boolean enabled;
	private boolean emailVerified;
	private @GeneratedValue Instant created;
	private @GeneratedValue Instant updated;

	@Override
	public String toResourceType() {
		return "user";
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
		attrs.put("email", email);
		attrs.put("name", name);
		attrs.put("enabled", enabled);
		attrs.put("emailVerified", emailVerified);
		attrs.put("created", created);
		attrs.put("updated", updated);
		return attrs;
	}
}
