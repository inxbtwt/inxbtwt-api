package com.inxbtwt.infra.exception.http;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.infra.controller.search.SearchController;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(assignableTypes = SearchController.class)
public class SearchExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(HandlerMethodValidationException exception) {
        if (exception.isForReturnValue()) {
            return handleUnexpected(exception);
        }

        String message = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));

        return ApiControllerUtils.badRequest(message);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchApi(FeignException exception) {
        log.error("Falha na comunicação com a search-api (HTTP {})", exception.status());

        if (exception.status() == 400) {
            return ApiControllerUtils.badRequest("Parâmetros de busca inválidos.");
        }
        if (exception.status() == 429) {
            return ApiControllerUtils.tooManyRequests(HttpMessages.TOO_MANY_REQUESTS);
        }
        if (exception.status() < 0 || exception.status() == 503 || exception.status() == 504) {
            return ApiControllerUtils.serviceUnavailable();
        }

        return ApiControllerUtils.error(HttpStatus.BAD_GATEWAY, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        if (exception instanceof ErrorResponse error) {
            return ApiControllerUtils.error(error.getStatusCode(), null, error.getHeaders());
        }

        log.error("Erro inesperado na busca de usuários", exception);
        return ApiControllerUtils.internalServerError();
    }
}
