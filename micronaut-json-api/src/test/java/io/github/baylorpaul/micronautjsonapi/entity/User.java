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
	@Override
	public String toResourceType() {
		return "user";
	}

	private @Id @GeneratedValue @NonNull long id;
	private @NotBlank String email;
	private @NotBlank String name;
	private @JsonIgnore @Nullable String password;
	private boolean enabled;
	private boolean emailVerified;
	private @GeneratedValue Instant created;
	private @GeneratedValue Instant updated;
}
