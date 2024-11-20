package io.github.baylorpaul.micronautjsonapi.model;

import io.github.baylorpaul.micronautjsonapi.model.types.JsonApiLinkType;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.SequencedMap;

/** A <a href="https://jsonapi.org/">JSON:API</a> error. */
@Data
@SuperBuilder
@Serdeable
@NoArgsConstructor
@ReflectiveAccess
public class JsonApiError {
	/** a unique identifier for this particular occurrence of the problem. */
	private @Nullable String id;
	/** links related to the error object */
	private @Nullable SequencedMap<String, JsonApiLinkType> links;
	/** the HTTP status code applicable to this problem, expressed as a string value. This SHOULD be provided. */
	private @Nullable String status;
	/** an application-specific error code, expressed as a string value. */
	private @Nullable String code;
	/** a short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization. */
	private @Nullable String title;
	/** a human-readable explanation specific to this occurrence of the problem. Like title, this field's value can be localized. */
	private @Nullable String detail;
	/** an object containing references to the primary source of the error. It SHOULD include at least one member or be omitted. */
	private @Nullable JsonApiErrorSource source;
	/** a meta object containing non-standard meta-information about the error. */
	private @Nullable Object meta;
}
