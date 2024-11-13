package io.github.baylorpaul.micronautjsonapi.exceptions;

import io.github.baylorpaul.micronautjsonapi.model.JsonApiError;
import io.github.baylorpaul.micronautjsonapi.model.JsonApiErrorResponse;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.exceptions.response.Error;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Singleton;

/**
 * Similar to Micronaut's HateoasErrorResponseProcessor, but creates the error responses in JSON:API format instead.
 */
@Singleton
@Primary
public class JsonApiErrorResponseProcessor implements ErrorResponseProcessor<JsonApiErrorResponse> {

    @Override
    @NonNull
    public MutableHttpResponse<JsonApiErrorResponse> processResponse(
            @NonNull ErrorContext errorContext, @NonNull MutableHttpResponse<?> response
    ) {
        if (errorContext.getRequest().getMethod() == HttpMethod.HEAD) {
            return (MutableHttpResponse<JsonApiErrorResponse>) response;
        }
        JsonApiErrorResponse.JsonApiErrorResponseBuilder errorBuilder = JsonApiErrorResponse.builder()
                .message(response.reason());
        if (errorContext.hasErrors()) {
            for (Error jsonError : errorContext.getErrors()) {
                JsonApiError error = JsonApiError.builder()
                        .status(String.valueOf(response.code()))
                        .detail(jsonError.getMessage())
                        .build();
                errorBuilder.error(error);
            }
        }

        return response.body(errorBuilder.build()).contentType(MediaType.APPLICATION_JSON_TYPE);
    }
}
