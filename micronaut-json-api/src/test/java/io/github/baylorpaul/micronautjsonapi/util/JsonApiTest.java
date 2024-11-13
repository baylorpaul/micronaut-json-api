package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.entity.Article;
import io.github.baylorpaul.micronautjsonapi.entity.GrantingToken;
import io.github.baylorpaul.micronautjsonapi.entity.PhysicalAddress;
import io.github.baylorpaul.micronautjsonapi.entity.User;
import io.github.baylorpaul.micronautjsonapi.model.*;
import io.github.baylorpaul.micronautjsonapi.service.JsonApiService;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@MicronautTest
public class JsonApiTest {

	@Inject
	private JsonMapper jsonMapper;

	@Inject
	private JsonApiService jsonApiService;

	@Test
	public void testJson() {
		Assertions.assertEquals("null", toJson(null));
		Assertions.assertEquals("{\"aaa\":\"bbb\"}", toJson(Collections.singletonMap("aaa", "bbb")));

		Assertions.assertNull(fromJson("null", Map.class));

		Map<?,?> m = fromJson("{\"aaa\":\"bbb\"}", Map.class);
		Assertions.assertNotNull(m);
		Assertions.assertEquals("bbb", m.get("aaa"));
	}

	/**
	 * Since the "data" field on JsonApiObject can be either an object or an array of objects, test deserialization and
	 * serialization for JsonApiObject. In this case, we'll test where the "data" is a JsonApiResource (an object).
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
	@Test
	public void testJsonApiResourceSerialization(JsonMapper jsonMapper) throws IOException {
		String relationshipsJson = "{\"user\":{\"data\":{\"type\":\"user\",\"id\":\"8\",\"attributes\":{\"name\":\"Billy\"}}}}";
		String json = "{\"type\":\"grantingToken\",\"id\":\"5\",\"relationships\":" + relationshipsJson + "}";
		JsonApiResource res1 = jsonMapper.readValue(json, JsonApiResource.class);
		Assertions.assertNotNull(res1);
		Map<String, JsonApiObject<? extends JsonApiDataType>> relationships = res1.getRelationships();
		Assertions.assertNotNull(res1.getRelationships());
		Assertions.assertNotNull(relationships);
		Assertions.assertEquals(1, relationships.keySet().size());
		Assertions.assertEquals("user", relationships.keySet().stream().findFirst().orElse(null));
		JsonApiObject<?> val = relationships.get("user");
		Assertions.assertNotNull(val);
		JsonApiDataType dt = val.getData();
		Assertions.assertNotNull(dt);
		Assertions.assertEquals(JsonApiResource.class, dt.getClass());
		JsonApiResource res2 = (JsonApiResource) dt;
		Assertions.assertEquals("8", res2.getId());
		Assertions.assertEquals("user", res2.getType());
		Map<String, Object> attrs = res2.getAttributes();
		Assertions.assertNotNull(attrs);
		Assertions.assertEquals("Billy", attrs.get("name"));

		// Now test serialization
		Assertions.assertEquals(json, toJson(res1));
	}

	/**
	 * Ensure that the "data" property in JsonApiTopLevelArray is able to be interpreted as an array, not an object.
	 */
	@Test
	public void testWriteReadJsonApiTopLevelArray() {
		User user1 = User.builder()
				.id(555)
				.email("joe@example.com")
				.name("Joe")
				.build();
		GrantingToken gt = GrantingToken.builder()
				.id(333)
				.user(user1)
				.token("abcdef")
				.revoked(false)
				.lastUsedDate(null)
				.expirationDate(Instant.now().plus(1L, ChronoUnit.DAYS))
				.build();

		String result = toJson(
				JsonApiTopLevelArray.topLevelBuilder()
						.data(new JsonApiArray(Collections.singletonList(gt)))
						.included(new JsonApiArray(Collections.singletonList(user1)))
						.build()
		);
		JsonApiTopLevelArray tlArr = fromJson(result, JsonApiTopLevelArray.class);
		Assertions.assertNotNull(tlArr);
		Assertions.assertEquals(1, tlArr.getData().size());

		JsonApiResource res = tlArr.getData().get(0);
		Assertions.assertEquals("grantingToken", res.getType());
		Assertions.assertEquals("333", res.getId());

		GrantingToken grantingToken2 = jsonApiService.readResourceWithId(res, GrantingToken.class)
				.orElseThrow(() -> new RuntimeException("Expected to find granting token"));

		Assertions.assertEquals(333L, grantingToken2.getId());
		Assertions.assertNull(grantingToken2.getToken());
		Assertions.assertFalse(grantingToken2.isRevoked());
		Assertions.assertNull(grantingToken2.getLastUsedDate());
		Assertions.assertNotNull(grantingToken2.getExpirationDate());

		User userRel = grantingToken2.getUser();
		Assertions.assertEquals(555L, userRel.getId());
		// We won't expect any other properties, because relationships only provide the "type" and "ID".
		// The other properties may be found in the "included".
		Assertions.assertNull(userRel.getEmail());
		Assertions.assertNull(userRel.getName());

		Assertions.assertNotNull(tlArr.getIncluded());
		Assertions.assertEquals(1, tlArr.getIncluded().size());

		JsonApiResource userRes = tlArr.getIncluded().getFirst();
		Assertions.assertEquals("user", userRes.getType());
		Assertions.assertEquals("555", userRes.getId());

		User user2 = jsonApiService.readResourceWithId(userRes, User.class)
				.orElseThrow(() -> new RuntimeException("Expected to find user"));
		Assertions.assertEquals(555L, user2.getId());
		Assertions.assertEquals("joe@example.com", user2.getEmail());
		Assertions.assertEquals("Joe", user2.getName());
	}

	/**
	 * Ensure that the "data.relationships" property can handle either a JsonApiResourceIdentifier or JsonApiArray for
	 * each relationship's "data" attribute. See example on home page of https://jsonapi.org/, where "comments" is an
	 * array.
	 */
	@Test
	public void testReadWriteJsonApiRelationships() {
		String authorDataJson = "{\"type\":\"user\",\"id\":\"9\"}";
		String addressesDataJson = "[" +
					"{\"type\":\"physicalAddress\",\"id\":\"5\"}," +
					"{\"type\":\"physicalAddress\",\"id\":\"12\"}" +
				"]";
		String dataJsonFields1 = "\"type\":\"article\"," +
				"\"id\":\"10\",";
		String dataJsonFields2 = "\"relationships\":{" +
					"\"addresses\":{" +
						"\"data\":" + addressesDataJson +
					"}," +
					"\"author\":{" +
						"\"data\":" + authorDataJson +
					"}" +
				"}";
		String metaJson = "\"meta\":{\"copyright\":\"Copyright 2024 Example Corp.\"}";
		String inputJson = "{" +
					"\"data\":[{" +
						dataJsonFields1 +
						dataJsonFields2 +
					"}]" +
					"," + metaJson +
				"}";
		String expectedJson = "{" +
					"\"data\":[{" +
						dataJsonFields1 +
						"\"attributes\":{}," +
						dataJsonFields2 +
					"}]" +
				"}";

		JsonApiObject<JsonApiArray> body = fromJson(inputJson, JsonApiObject.class);
		Assertions.assertNotNull(body);
		JsonApiArray topData = body.getData();
		Assertions.assertNotNull(topData);

		Assertions.assertNotNull(topData);
		Assertions.assertEquals(1, topData.size());
		JsonApiResource res1 = topData.getFirst();
		Assertions.assertEquals("article", res1.getType());
		Assertions.assertEquals("10", res1.getId());
		Map<String, JsonApiObject<? extends JsonApiDataType>> relationships = res1.getRelationships();
		Assertions.assertNotNull(relationships);

		JsonApiResource authorData = (JsonApiResource) relationships.get("author").getData();
		Assertions.assertNotNull(authorData);
		Assertions.assertEquals("user", authorData.getType());
		Assertions.assertEquals("9", authorData.getId());

		JsonApiArray addressesData = (JsonApiArray) relationships.get("addresses").getData();
		Assertions.assertEquals(2, addressesData.size());
		JsonApiResource addrRes1 = addressesData.get(0);
		JsonApiResource addrRes2 = addressesData.get(1);
		Assertions.assertEquals("physicalAddress", addrRes1.getType());
		Assertions.assertEquals("5", addrRes1.getId());
		Assertions.assertEquals("physicalAddress", addrRes2.getType());
		Assertions.assertEquals("12", addrRes2.getId());

		Map<String, Object> meta = body.getMeta();
		Assertions.assertNotNull(meta);
		Assertions.assertEquals("Copyright 2024 Example Corp.", meta.get("copyright"));

		List<Article> articles = jsonApiService.readDataWithIds(body, Article.class);
		Assertions.assertEquals(1, articles.size());
		Article article = articles.getFirst();
		Assertions.assertEquals("10", article.getId());

		User author = article.getAuthor();
		Assertions.assertEquals(9L, author.getId());

		List<PhysicalAddress> addrs = article.getAddresses();
		Assertions.assertEquals(2, addrs.size());
		PhysicalAddress addr1 = addrs.get(0);
		PhysicalAddress addr2 = addrs.get(1);
		Assertions.assertEquals(5L, addr1.getId());
		Assertions.assertEquals(12L, addr2.getId());

		// Now test serialization
		final JsonApiArray data = new JsonApiArray(articles);
		final JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(null, null);
		final JsonApiTopLevelObject<?> tlArr = includeProcessor.asTopLevelObject(data);

		Assertions.assertEquals(expectedJson, toJson(tlArr));
	}

	@Test
	public void testReadingJsonApiResource() {
		String attrsJson = "{\"email\":\"john@example.com\",\"name\":\"John\"}";
		String dataJsonFields1 = "\"type\":\"user\",\"id\":\"54\",\"attributes\":" + attrsJson;
		String json = "{\"data\":{" + dataJsonFields1 + "}}";

		JsonApiObject<JsonApiResource> body = fromJson(json, JsonApiObject.class);
		JsonApiResource userData = body.getData();
		Assertions.assertNotNull(userData);
		Assertions.assertEquals("user", userData.getType());
		Assertions.assertEquals("54", userData.getId());

		User user = jsonApiService.readDataWithId(body, User.class);
		Assertions.assertNotNull(user);
		Assertions.assertEquals(54L, user.getId());
		Assertions.assertEquals("john@example.com", user.getEmail());
		Assertions.assertEquals("John", user.getName());
	}

	private <T> T fromJson(String str, Class<T> clazz) {
		try {
			return jsonMapper.readValue(str, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Unable to map JSON string [class: " + clazz.getCanonicalName() + ", str: " + str + "]", e);
		}
	}

	private String toJson(Object obj) {
		try {
			return obj == null ? "null" : jsonMapper.writeValueAsString(obj);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write object as JSON", e);
		}
	}
}
