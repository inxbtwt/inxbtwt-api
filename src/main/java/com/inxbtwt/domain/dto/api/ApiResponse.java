package com.inxbtwt.domain.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.opentelemetry.api.trace.StatusCode;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String message,
        T data,
        StatusCode status
) {
}
