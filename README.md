# Micronaut JSON:API library

A [JSON:API](https://jsonapi.org/) library for [Micronaut](https://micronaut.io/) implementations.

## API implementation with JSON:API

### Setup

In your `application.properties`, set `micronaut.serde.serialization.inclusion=non_absent`. This will continue omitting null and Optional.empty(), but for the JSON:API spec, include empty collections.
I.e. it is desirable to have `"attributes":{}` instead of excluding `attributes`.

### Retrieve a record

See the samples in the test packages, such as `User.java` or `GrantingToken.java`. For entities that you want to expose to
your API:
1. Implement the JsonApiResourceable interface on your entity.
2. Ensure the value in `toResourceType()` is unique.
3. Expose the publicly accessible attributes in the `toJsonApiAttributes()` method.
4. If applicable, implement the `toRelationships()` method.
5. In your controller method, find the result, and map it to a `JsonApiTopLevelResource` via `JsonApiResourceable::toTopLevelResource`. E.g.
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
	UserCreation dto = jsonApiService.readDataWithoutId(body, UserCreation.class);
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
	GrantingToken dto = jsonApiService.readDataWithoutId(body, GrantingToken.class);
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
User dto = jsonApiService.readValue(res.getAttributes(), User.class);
```

### Delete a record

```java
@Secured(SecurityRule.IS_AUTHENTICATED)
@Delete("/grantingTokens/{id}")
public HttpResponse<?> delete(long id, Principal principal) {
	// delete the record
}
```
