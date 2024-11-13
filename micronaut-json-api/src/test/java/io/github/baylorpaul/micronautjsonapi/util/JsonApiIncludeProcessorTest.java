package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.entity.GrantingToken;
import io.github.baylorpaul.micronautjsonapi.entity.TokenReferencingEntity;
import io.github.baylorpaul.micronautjsonapi.entity.User;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiResource;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiTopLevelResource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.exceptions.HttpStatusException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.github.baylorpaul.micronautjsonapi.util.JsonApiIncludeProcessor.RelationshipRetriever;

public class JsonApiIncludeProcessorTest {

	@Test
	void testUnsupportedPaths() {
		List<RelationshipRetriever> supportedIncludePaths = buildEmptyRelationshipRetrievers(
				"author", "author.address", "publishingCompany"
		);
		String rawInclude = "blamo,author";
		try {
			new JsonApiIncludeProcessor(rawInclude, supportedIncludePaths);

			Assertions.fail("Expected an exception");
		} catch (HttpStatusException e) {
			// do nothing - this is expected
		}
	}

	@Test
	void testSupportedPaths() {
		List<RelationshipRetriever> supportedIncludePaths = buildEmptyRelationshipRetrievers(
				"author", "author.address", "publishingCompany"
		);
		String rawInclude = "author.address";

		JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(rawInclude, supportedIncludePaths);
		assertValidIncludesMatches(
				includeProcessor,
				"author",
				"author.address"
		);
	}

	@Test
	void testRedundantPaths() {
		List<RelationshipRetriever> supportedIncludePaths = buildEmptyRelationshipRetrievers(
				"author", "author.address", "publishingCompany", "agent", "agent.address"
		);
		String rawInclude = "agent.address,author,author.address,author.address,agent";

		JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(rawInclude, supportedIncludePaths);
		assertValidIncludesMatches(
				includeProcessor,
				"agent",
				"agent.address",
				"author",
				"author.address"
		);
	}

	@Test
	void testRedundantParents() {
		List<RelationshipRetriever> supportedIncludePaths = buildEmptyRelationshipRetrievers(
				"author", "author.address", "author.info"
		);
		String rawInclude = "author.info,author.address";

		JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(rawInclude, supportedIncludePaths);
		assertValidIncludesMatches(
				includeProcessor,
				"author",
				"author.address",
				"author.info"
		);
	}

	@Test
	void testSpacesInInclude() {
		List<RelationshipRetriever> supportedIncludePaths = buildEmptyRelationshipRetrievers(
				"vehicle", "vehicle.owner", "vehicle.owner.address", "vehicle.manufacturer", "company"
		);
		String rawInclude = "vehicle.owner.address, vehicle.manufacturer, company";

		JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(rawInclude, supportedIncludePaths);
		assertValidIncludesMatches(
				includeProcessor,
				"company",
				"vehicle",
				"vehicle.manufacturer",
				"vehicle.owner",
				"vehicle.owner.address"
		);
	}

	@Test
	void testRelationshipIncludesProcessing() {
		User fakeUser = User.builder().id(555L).build();
		GrantingToken fakeToken = GrantingToken.builder().id(444L).user(fakeUser).build();
		List<RelationshipRetriever> supportedIncludePaths = Arrays.asList(
				new RelationshipRetriever("familyGrantingToken", ids -> Collections.singletonList(fakeToken)),
				new RelationshipRetriever("familyGrantingToken.user", ids -> Collections.singletonList(fakeUser))
		);
		String rawInclude = "familyGrantingToken.user";

		JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(rawInclude, supportedIncludePaths);
		assertValidIncludesMatches(
				includeProcessor,
				"familyGrantingToken",
				"familyGrantingToken.user"
		);

		GrantingToken familyGrantingToken = GrantingToken.builder().id(777L).build();
		TokenReferencingEntity tre = TokenReferencingEntity.builder()
				.id("fake-id")
				.familyGrantingToken(familyGrantingToken)
				.build();

		JsonApiTopLevelResource tlr = includeProcessor.asTopLevelObject(tre);
		Assertions.assertNotNull(tlr.getIncluded());
		Assertions.assertEquals(2, tlr.getIncluded().size());

		int i = -1;
		JsonApiResource res1 = tlr.getIncluded().get(++i);
		JsonApiResource res2 = tlr.getIncluded().get(++i);

		Assertions.assertEquals("grantingToken", res1.getType());
		Assertions.assertEquals(Long.toString(fakeToken.getId()), res1.getId());
		Assertions.assertEquals("user", res2.getType());
		Assertions.assertEquals(Long.toString(fakeUser.getId()), res2.getId());
	}

	private static List<RelationshipRetriever> buildEmptyRelationshipRetrievers(String... includePaths) {
		List<RelationshipRetriever> list = new ArrayList<>(includePaths.length);
		for (String includePath : includePaths) {
			list.add(new RelationshipRetriever(includePath, ids -> Collections.emptyList()));
		}
		return list;
	}

	private void assertValidIncludesMatches(
			@NonNull JsonApiIncludeProcessor includeProcessor,
			@NonNull String... expectedValidIncludes
	) {
		List<RelationshipRetriever> validIncludes = includeProcessor.getValidIncludes();

		Assertions.assertNotNull(validIncludes);
		Assertions.assertEquals(expectedValidIncludes.length, validIncludes.size());

		int i = 0;
		for (String expectedValidInclude : expectedValidIncludes) {
			Assertions.assertEquals(expectedValidInclude, validIncludes.get(i).includePath());
			i++;
		}
	}
}
