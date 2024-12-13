package io.github.baylorpaul.micronautjsonapi.entity;

import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Relation;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A dummy entity used to test multi-depth JSON:API paths to "include". E.g. if the API method is provided an "include"
 * param with value a value of "familyGrantingToken.user".
 */
@Data
@Builder(toBuilder = true)
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor
@ReflectiveAccess
public class Article implements JsonApiResourceable {
	private @Id @NonNull String id;
	private @Relation(Relation.Kind.MANY_TO_ONE) User author;
	private @Relation(Relation.Kind.ONE_TO_MANY) List<PhysicalAddress> addresses;

	@Override
	public String toResourceType() {
		return "article";
	}

	@Override
	public String toJsonApiId() {
		return id;
	}

	@Override
	public void applyJsonApiId(String jsonApiId) {
		setId(jsonApiId == null ? "0" : jsonApiId);
	}
}
