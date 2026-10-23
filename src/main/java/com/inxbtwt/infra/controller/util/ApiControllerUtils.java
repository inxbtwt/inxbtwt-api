package com.inxbtwt.infra.controller.util;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.infra.exception.http.HttpMessages;
import io.opentelemetry.api.trace.StatusCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class ApiControllerUtils {
    private ApiControllerUtils() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return success(HttpStatus.CREATED, message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> accepted(String message, T data) {
        return success(HttpStatus.ACCEPTED, message, data);
    }
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatusCode status, String message, T data) {
        if (!status.is2xxSuccessful() || status.value() == 204 || status.value() == 205) {
            throw new IllegalArgumentException("Informe um status de sucesso que permita corpo na resposta.");
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.<T>builder().message(message).data(data).status(StatusCode.OK).build());
    }

    public static ResponseEntity<ApiResponse<Void>> error(HttpStatusCode status, String message) {
        return error(status, message, HttpHeaders.EMPTY);
    }

    public static ResponseEntity<ApiResponse<Void>> error(HttpStatusCode status, String message, HttpHeaders headers) {
        if (!status.isError()) {
            throw new IllegalArgumentException("Informe um status HTTP de erro.");
        }
        String publicMessage = status.is5xxServerError() || message == null || message.isBlank()
                ? HttpMessages.forStatus(status) : message;
        return ResponseEntity.status(status).headers(headers).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.<Void>builder().message(publicMessage).status(StatusCode.ERROR).build());
    }

    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public static ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }

    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    public static ResponseEntity<ApiResponse<Void>> methodNotAllowed(String message) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    public static ResponseEntity<ApiResponse<Void>> notAcceptable(String message) {
        return error(HttpStatus.NOT_ACCEPTABLE, message);
    }

    public static ResponseEntity<ApiResponse<Void>> conflict(String message) {
        return error(HttpStatus.CONFLICT, message);
    }

    public static ResponseEntity<ApiResponse<Void>> payloadTooLarge(String message) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, message);
    }

    public static ResponseEntity<ApiResponse<Void>> unsupportedMediaType(String message) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }

    public static ResponseEntity<ApiResponse<Void>> tooManyRequests(String message) {
        return error(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    public static ResponseEntity<ApiResponse<Void>> internalServerError() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    public static ResponseEntity<ApiResponse<Void>> serviceUnavailable() {
        return error(HttpStatus.SERVICE_UNAVAILABLE, null);
    }
}

