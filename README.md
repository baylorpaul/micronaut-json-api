# Micronaut JSON:API library

A [JSON:API](https://jsonapi.org/) library for [Micronaut](https://micronaut.io/) implementations.

## Features

1. Entity mapping to JSON:API resource(s)
2. A processor for an "include" query parameter. E.g. `?include=author.address,publishingCompany`
3. Error response processor
4. Page and Slice responses
5. Deserializing "data" fields into corresponding JsonApiResource or JsonApiArray
6. Transform JSON:API resource(s) into other Java classes, such as entities

## API implementation with JSON:API

### Setup

#### Add Dependency to your `build.gradle`
```groovy
dependencies {
    implementation("io.github.baylorpaul:micronaut-json-api:2.0.0")
}
```

#### Update Application Serialization
In your `application.properties`, set:

	micronaut.serde.serialization.inclusion=non_absent

This will continue omitting null and Optional.empty(), but for the JSON:API spec, include empty collections.
I.e. it is desirable to have `"attributes":{}` instead of excluding `attributes`.

### Update entities

For entities you plan to expose to the API, add `implements JsonApiResourceable` to the class. And then add the appropriate methods. E.g.
```java
@MappedEntity
@Data
@Builder(toBuilder = true)
@Serdeable.Deserializable
@NoArgsConstructor
@AllArgsConstructor
@ReflectiveAccess
public class Article implements JsonApiResourceable {
	private @Id @GeneratedValue @NonNull long id;
	private @JsonIgnore @Nullable String topSecretValue;
	private @Relation(Relation.Kind.MANY_TO_ONE) User author;
	private @Relation(Relation.Kind.ONE_TO_MANY) List<PhysicalAddress> addresses;

	@Override
	public String toResourceType() {
		return "article";
	}

	@Override
	public String toJsonApiId() {
		return Long.toString(id);
	}

	@Override
	public void applyJsonApiId(String jsonApiId) {
		setId(jsonApiId == null ? 0L : Long.parseLong(jsonApiId));
	}
}
```

### Retrieve a record

See the samples in the test packages, such as `User.java` or `GrantingToken.java`. For entities that you want to expose
to your API:
1. Implement the JsonApiResourceable interface on your entity.
2. Ensure the value in `toResourceType()` is unique.
3. Apply the `@Id` annotation to your ID field. This will hide it from the JSON:API attributes.
4. Apply the `@Relationship` annotation to each relationship field.
5. Add the `@JsonIgnore` annotation to fields you do not want to expose as JSON:API attributes or relationships.
6. In your controller method, find the result, and map it to a `JsonApiTopLevelResource` via `JsonApiResourceable::toTopLevelResource`. E.g.
	```java
	@Secured(SecurityRule.IS_AUTHENTICATED)
	@Get("/users/{id}")
	public Optional<JsonApiTopLevelResource> show(long id, Principal principal) {
		if (id != SecurityUtil.requireUserId(principal)) {
			throw new HttpStatusException(HttpStatus.NOT_FOUND, "id does not match the requesting user");
		}
		return userRepo.findById(id)
				.map(JsonApiResourceable::toTopLevelResource);
	}
	```

### Fetching Includes

As documented in [JSON:API's Inclusion of Related Resources](https://jsonapi.org/format/#fetching-includes), "an endpoint MAY also support an `include` query parameter to allow the client to customize which related resources should be returned." 
To fulfill this, use `JsonApiIncludeProcessor`. E.g.
```java
List<RelationshipRetriever> supportedIncludePaths = buildEmptyRelationshipRetrievers(
	"author", "author.address", "publishingCompany"
);
JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(includeQueryParameter, supportedIncludePaths);
```
In a controller method, you may e.g.
```java
@Secured(SecurityRule.IS_AUTHENTICATED)
@Get("/grantingTokens/{?filterOptions*}")
public JsonApiPage<GrantingToken> getGrantingTokens(
		Principal principal,
		@Valid GrantingTokenFilterOptions filterOptions,
		@Valid Pageable pageable,
		@Nullable String include
) {
	// Don't allow integration tokens to view tokens
	SecurityUtil.throwIfNotAccessTokenAuthorization(principal);

	// In this example, we don't support any values for "include"
	List<RelationshipRetriever> supportedIncludePaths = null;

	final GrantingTokenFilter filter = filterOptions.buildFilterAsPermitted(principal);
	final Page<GrantingToken> page = grantingTokenRepo.getGrantingTokens(filter, pageable);
	JsonApiIncludeProcessor includeProcessor = new JsonApiIncludeProcessor(include, supportedIncludePaths);
	return new JsonApiPage<>(page, includeProcessor::findIncluded);
}
```

### Create a record

#### Simple creation

The `@Body` may be mapped to an entity or a different DTO.
```java
@Secured(SecurityRule.IS_ANONYMOUS) // anyone can make a new user
@Post("/users")
@Status(HttpStatus.CREATED)
public JsonApiTopLevelResource create(@Body JsonApiObject<JsonApiResource> body) {
	UserCreation dto = JsonApiUtil.readDataWithoutId(jsonMapper, body, UserCreation.class);
	User newUser = userRestService.createUser(dto);
	return newUser.toTopLevelResource();
}
```
In the previous example, `UserCreation` is the following, but you can also map to your entity:
```java
@Data
@Serdeable
@ReflectiveAccess
public class UserCreation {
	private @NotBlank String email;
	private @Nullable String name;
	private @NotBlank String password;
}
```

#### Create with a `JsonApiObject` and provide additional non-standard attributes

```java
@Secured(SecurityRule.IS_AUTHENTICATED)
@Post("/grantingTokens")
@Status(HttpStatus.CREATED)
public JsonApiTopLevelResource create(Principal principal, @Body JsonApiObject<JsonApiResource> body) {
	// Don't allow integration tokens to create other tokens
	SecurityUtil.throwIfNotAccessTokenAuthorization(principal);

	long userId = SecurityUtil.requireUserId(principal);
	GrantingToken dto = JsonApiUtil.readDataWithoutId(jsonMapper, body, GrantingToken.class);
	TokenCreation tokenCreation = grantingTokenRestService.createToken(userId, dto);

	JsonApiResource res = tokenCreation.grantingToken().toResource();
	// Include the signed token when it is created, but never again
	res.putAllAttributes(new TreeMap<>(Map.of("signedToken", tokenCreation.signedToken())));

	return JsonApiTopLevelResource.topLevelResourceBuilder()
			.data(res)
			.build();
}
```

### Update a record

```java
@Secured(SecurityRule.IS_AUTHENTICATED)
@Patch("/users/{id}")
public Optional<JsonApiTopLevelResource> update(long id, Principal principal, @Body JsonApiObject<JsonApiResource> body) {
	return JsonApiUtil.readAndValidateLongId(body, id)
			.map(bodyId -> bodyId.longValue() == SecurityUtil.requireUserId(principal) ? bodyId : null)
			.flatMap(bodyId -> userRepo.findById(bodyId))
			.map(user -> userRestService.updateUser(user, body.getData()))
			.map(JsonApiResourceable::toTopLevelResource);
}
```
Inside `updateUser()`, you may map the provided `JsonApiResource` to an entity or DTO via the following as long as `res.getAttributes()` is not null:
```java
User dto = JsonApiUtil.readValue(jsonMapper, res.getAttributes(), User.class);
```

### Delete a record

```java
@Secured(SecurityRule.IS_AUTHENTICATED)
@Delete("/grantingTokens/{id}")
public HttpResponse<?> delete(long id, Principal principal) {
	// delete the record
}
```

## Need a TypeScript client?

If you're looking for a TypeScript client, consider using [JSON:API Bridge](https://github.com/baylorpaul/json-api-bridge).
The interfaces and types are modeled after the DTOs in this project.
