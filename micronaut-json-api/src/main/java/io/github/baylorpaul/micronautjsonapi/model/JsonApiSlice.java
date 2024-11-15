package io.github.baylorpaul.micronautjsonapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.model.Slice;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.Function;

/**
 * A JSON:API representation of a slice. This models a type that supports pagination operations.
 * A slice is a result list associated with a particular Pageable
 * @param <T> - The generic type
 */
@Serdeable
@ReflectiveAccess
public class JsonApiSlice<T extends JsonApiResourceable> extends JsonApiTopLevelObject<JsonApiArray> {

	private final Slice<T> slice;

	/**
	 * Create a JSON:API slice
	 * @param slice a result list associated with a particular Pageable
	 */
	public JsonApiSlice(Slice<T> slice) {
		this(slice, null);
	}

	/**
	 * Create a JSON:API slice
	 * @param slice a result list associated with a particular Pageable
	 * @param findIncluded null for none, else a function to find included values for the collection of resources
	 */
	public JsonApiSlice(Slice<T> slice, Function<Collection<? extends JsonApiResource>, JsonApiArray> findIncluded) {
		this.slice = slice;
		final JsonApiArray data = new JsonApiArray(slice.getContent());

		final SequencedMap<String, Object> meta = new LinkedHashMap<>();
		meta.put("pageNumber", getPageNumber());
		meta.put("pageSize", getPageSize());

		setMeta(meta);
		setData(data);

		if (findIncluded != null) {
			setIncluded(findIncluded.apply(data));
		}
	}

	/**
	 * Find the slice content.
	 * @return The slice content.
	 */
	@JsonIgnore
	public List<T> getContent() {
		return slice.getContent();
	}

	/**
	 * Find the page number, zero-based
	 * @return The page number, zero-based
	 */
	@JsonIgnore
	public int getPageNumber() {
		return slice.getPageNumber();
	}

	/**
	 * Find the page size of the slice.
	 * @return The page size of the slice.
	 */
	@JsonIgnore
	public int getPageSize() {
		return slice.getSize();
	}
}
