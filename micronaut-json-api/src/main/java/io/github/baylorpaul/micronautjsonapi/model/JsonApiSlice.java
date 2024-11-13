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

@Serdeable
@ReflectiveAccess
public class JsonApiSlice<T extends JsonApiResourceable> extends JsonApiTopLevelObject<JsonApiArray> {

	private final Slice<T> slice;

	public JsonApiSlice(Slice<T> slice) {
		this(slice, null);
	}

	/**
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
	 * @return The content.
	 */
	@JsonIgnore
	public List<T> getContent() {
		return slice.getContent();
	}

	/**
	 * @return The page number, zero-based
	 */
	@JsonIgnore
	public int getPageNumber() {
		return slice.getPageNumber();
	}

	/**
	 * @return The page size of the slice.
	 */
	@JsonIgnore
	public int getPageSize() {
		return slice.getSize();
	}
}
