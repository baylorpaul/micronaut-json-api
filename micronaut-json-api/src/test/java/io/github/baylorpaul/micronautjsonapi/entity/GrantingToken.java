package io.github.baylorpaul.micronautjsonapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Granting tokens, such as (1) refresh tokens that may generate a new access token when an older access token expires,
 * or (2) integration API bearer tokens.
 */
@MappedEntity
@Data
@Builder(toBuilder = true)
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor
@ReflectiveAccess
public class GrantingToken implements JsonApiResourceable {

	private @Id @GeneratedValue @NonNull long id;
	private @Relation(Relation.Kind.MANY_TO_ONE) User user;
	private @JsonIgnore @NonNull @NotBlank String token;
	private @Nullable String comment;
	private boolean revoked;
	private @Nullable Instant lastUsedDate;
	private @Nullable Instant expirationDate;
	private @GeneratedValue Instant created;
	private @GeneratedValue Instant updated;

	@Override
	public String toResourceType() {
		return "grantingToken";
	}
}
