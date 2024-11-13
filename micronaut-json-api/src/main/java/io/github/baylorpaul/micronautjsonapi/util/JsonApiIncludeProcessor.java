package io.github.baylorpaul.micronautjsonapi.util;

import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.github.baylorpaul.micronautjsonapi.model.*;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read and process the "include" param according to <a href="https://jsonapi.org/">JSON:API</a>.
 */
public class JsonApiIncludeProcessor {

	/**
	 * @param includePath the "include" path, such as "author.address"
	 * @param idsToResources a function that receives the IDs for the "include" path, and returns the resources
	 */
	public record RelationshipRetriever(String includePath, Function<Collection<String>, Collection<? extends JsonApiResourceable>> idsToResources) {}

	private final List<RelationshipRetriever> validIncludes;

	/**
	 * @param rawInclude the raw "include" query parameter, as described by JSON:API. E.g. "author" or
	 *            "author.address,publishingCompany" or "author&fields[articles]=title,body&fields[people]=name"
	 * @param supportedIncludePaths supported include paths, such as ["author", "author.address", "publishingCompany"],
	 *            and corresponding functions to map IDs to resources.
	 *            If a value such as "author.address" is supported, then "author" must also be included as a supported path.
	 * @throws HttpStatusException if the include query parameter includes unsupported values
	 */
	public JsonApiIncludeProcessor(String rawInclude, Collection<RelationshipRetriever> supportedIncludePaths) throws HttpStatusException {
		this.validIncludes = validateIncludes(rawInclude, supportedIncludePaths);
	}

	List<RelationshipRetriever> getValidIncludes() {
		return validIncludes;
	}

	/**
	 * Parse the raw include query parameter, and validate it against supported includes
	 * @return null for none, else the valid include values sorted alphabetically by the include path, such as ["author", "author.address"]
	 * @throws HttpStatusException if the include query parameter includes unsupported values
	 */
	private static List<RelationshipRetriever> validateIncludes(String rawInclude, Collection<RelationshipRetriever> supportedIncludePaths) throws HttpStatusException {
		if (rawInclude == null || rawInclude.trim().isEmpty()) {
			return null;
		} else if (supportedIncludePaths == null) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Found 'include' value(s), but none are supported");
		}
		String[] paths = rawInclude.split(",");

		Map<String, RelationshipRetriever> map = new HashMap<>();
		for (String path : paths) {
			findParentPaths(path.trim(), supportedIncludePaths)
					.forEach(rr -> map.put(rr.includePath(), rr));
		}
		List<RelationshipRetriever> includes = new LinkedList<>(map.values());
		includes.sort(Comparator.comparing(RelationshipRetriever::includePath));
		return includes;
	}

	private static List<RelationshipRetriever> findParentPaths(String path, Collection<RelationshipRetriever> supportedIncludePaths) {
		List<RelationshipRetriever> paths = new LinkedList<>();
		int idx = path.lastIndexOf(".");
		if (idx >= 0) {
			paths.addAll(findParentPaths(path.substring(0, idx), supportedIncludePaths));
		}
		RelationshipRetriever rr = supportedIncludePaths.stream().filter(p -> p.includePath().equals(path)).findFirst()
				.orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "unsupported 'include' value: [" + path + "]"));
		paths.add(rr);
		return paths;
	}

	/**
	 * Look up values to include for the provided resources
	 * @param resources the resources for which to find included values, or null for no resources
	 */
	public JsonApiArray findIncluded(Collection<? extends JsonApiResource> resources) {
		final JsonApiArray included = validIncludes == null ? null : new JsonApiArray();
		if (resources != null && validIncludes != null) {
			included.addAll(processRelationshipIncludes(resources, validIncludes));
		}
		return included;
	}

	/**
	 * @param entity the entity to convert to a top level object, or null for none
	 */
	public JsonApiTopLevelResource asTopLevelObject(JsonApiResourceable entity) {
		final JsonApiResource data = entity == null ? null : entity.toResource();
		return asTopLevelObject(data);
	}

	/**
	 * @param entities the entities to convert to a top level object, or null for none
	 */
	public JsonApiTopLevelObject<?> asTopLevelObject(Collection<? extends JsonApiResourceable> entities) {
		final JsonApiArray data = entities == null ? null : new JsonApiArray(entities);
		return asTopLevelObject(data);
	}

	/**
	 * @param data the resource data, or null for none
	 */
	public JsonApiTopLevelResource asTopLevelObject(JsonApiResource data) {
		final JsonApiArray included = findIncluded(data == null ? null : Collections.singleton(data));
		return JsonApiTopLevelResource.topLevelResourceBuilder()
				.data(data)
				.included(included)
				.build();
	}

	/**
	 * @param arr the resource array, or null for none
	 */
	public JsonApiTopLevelObject<?> asTopLevelObject(JsonApiArray arr) {
		final JsonApiArray included = findIncluded(arr);
		return JsonApiTopLevelArray.topLevelBuilder()
				.data(arr)
				.included(included)
				.build();
	}

	private static LinkedList<JsonApiResource> processRelationshipIncludes(
			@NonNull Collection<? extends JsonApiResourceIdentifier> resources, @NonNull List<RelationshipRetriever> validIncludesForCurrentEntity
	) {
		LinkedList<JsonApiResource> result = new LinkedList<>();

		final int size = validIncludesForCurrentEntity.size();
		int i = 0;
		while (i < size) {
			final RelationshipRetriever relationshipInclude = validIncludesForCurrentEntity.get(i);
			final List<RelationshipRetriever> childrenIncludes = new LinkedList<>();
			RelationshipRetriever nextInclude;
			do {
				i++;
				nextInclude = i < size ? validIncludesForCurrentEntity.get(i) : null;
				if (nextInclude != null) {
					final String startsWith = relationshipInclude.includePath() + ".";
					if (nextInclude.includePath().startsWith(startsWith)) {
						// Strip out the parent path, and add the sub-path as a child include
						childrenIncludes.add(new RelationshipRetriever(
								nextInclude.includePath().substring(startsWith.length()),
								nextInclude.idsToResources()
						));
					} else {
						nextInclude = null;
					}
				}
			} while (nextInclude != null);

			// Look up IDs for the "relationshipInclude". If non-empty, retrieve the JsonApiResources for those.
			List<String> relationshipIds = findRelationshipIds(resources, relationshipInclude);
			Collection<JsonApiResource> relationships = null;
			if (!relationshipIds.isEmpty()) {
				relationships = relationshipInclude.idsToResources().apply(relationshipIds)
						.stream().map(JsonApiResourceable::toResource).collect(Collectors.toList());
			}

			//log.info("---------------------- " + relationshipInclude.includePath() + " --> " + childrenIncludes.stream().map(RelationshipRetriever::includePath).toList());

			if (!CollectionUtils.isEmpty(relationships)) {
				result.addAll(relationships);
				if (!childrenIncludes.isEmpty()) {
					result.addAll(processRelationshipIncludes(relationships, childrenIncludes));
				}
			}
		}
		return result;
	}

	private static List<String> findRelationshipIds(
			@NonNull Collection<? extends JsonApiResourceIdentifier> resources,
			@NonNull RelationshipRetriever relationshipInclude
	) {
		return resources.stream()
				.map(primaryResIdentifier -> {
					if (primaryResIdentifier instanceof JsonApiResource res) {
						if (res.getRelationships() != null) {
							JsonApiObject<? extends JsonApiDataType> relationship = res.getRelationships().get(relationshipInclude.includePath());
							JsonApiDataType data = relationship == null ? null : relationship.getData();
							if (data instanceof JsonApiResourceIdentifier dataResIdentifier) {
								return dataResIdentifier.getId();
							} else if (data instanceof JsonApiArray) {
								// do nothing - we don't do lookups for arrays
								return null;
							}
						}
					}
					return null;
				})
				.filter(Objects::nonNull)
				.toList();
	}
}
