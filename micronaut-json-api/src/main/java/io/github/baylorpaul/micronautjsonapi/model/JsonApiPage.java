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

/**
 * A JSON:API representation of a page. This models a type that supports pagination operations.
 * A Page is a result set associated with a particular Pageable that includes a calculation of the total size of page of
 * records.
 * @param <T> - The generic type
 */
@Serdeable
@ReflectiveAccess
public class JsonApiPage<T extends JsonApiResourceable> extends JsonApiSlice<T> {
	private final Page<T> page;

	/**
	 * Create a JSON:API page
	 * @param page a result set associated with a particular Pageable that includes a calculation of the total size of
	 *                page of records.
	 */
	public JsonApiPage(Page<T> page) {
		this(page, null);
	}

	/**
	 * Create a JSON:API page
	 * @param page a result set associated with a particular Pageable that includes a calculation of the total size of
	 *                page of records.
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
	 * Find the total number of pages
	 * @return The total number of pages
	 */
	@JsonIgnore
	public int getTotalPages() {
		return page.getTotalPages();
	}

	/**
	 * Find the total size of the all records.
	 * @return The total size of the all records.
	 */
	@JsonIgnore
	public long getTotalSize() {
		return page.getTotalSize();
	}
}
