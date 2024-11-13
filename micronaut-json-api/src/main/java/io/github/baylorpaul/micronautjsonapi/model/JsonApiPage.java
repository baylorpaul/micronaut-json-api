package io.github.baylorpaul.micronautjsonapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.baylorpaul.micronautjsonapi.identifiable.JsonApiResourceable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.data.model.Page;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.function.Function;

@Serdeable
@ReflectiveAccess
public class JsonApiPage<T extends JsonApiResourceable> extends JsonApiSlice<T> {
	private final Page<T> page;

	public JsonApiPage(Page<T> page) {
		this(page, null);
	}

	/**
	 * @param findIncluded null for none, else a function to find included values for the collection of resources
	 */
	public JsonApiPage(Page<T> page, Function<Collection<? extends JsonApiResource>, JsonApiArray> findIncluded) {
		super(page, findIncluded);
		this.page = page;

		SequencedMap<String, Object> meta = new LinkedHashMap<>(getMeta());
		meta.put("totalPages", getTotalPages());
		meta.put("totalSize", getTotalSize());
		setMeta(meta);
	}

	/**
	 * @return The total number of pages
	 */
	@JsonIgnore
	public int getTotalPages() {
		return page.getTotalPages();
	}

	/**
	 * @return The total size of the all records.
	 */
	@JsonIgnore
	public long getTotalSize() {
		return page.getTotalSize();
	}
}
