package com.inxbtwt.infra.exception.http;

import com.inxbtwt.infra.controller.user.UserController;
import com.inxbtwt.domain.dto.api.ApiResponse;
import io.opentelemetry.api.trace.StatusCode;
import lombok.extern.slf4j.Slf4j;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = UserController.class)
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttp(HttpException exception) {
        return error(exception.getStatus(), exception.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatusCode status, String message) {
        String publicMessage = status.is5xxServerError() || message == null || message.isBlank()
                ? HttpMessages.forStatus(status) : message;
        var response = ApiResponse.<Void>builder()
                .message(publicMessage)
                .status(StatusCode.ERROR)
                .build();
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyExists(UserAlreadyExistException exception) {
        return handleHttp(new ConflictHttpException(exception.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(UserNotFoundException exception) {
        return handleHttp(new NotFoundHttpException(exception.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException exception) {
        return handleHttp(new BadRequestHttpException(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
        return handleHttp(new BadRequestHttpException(message.isBlank() ? HttpMessages.BAD_REQUEST : message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(HttpMessageNotReadableException exception) {
        return handleHttp(new BadRequestHttpException(HttpMessages.INVALID_BODY));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        if (exception instanceof ErrorResponse frameworkError) {
            var response = error(frameworkError.getStatusCode(), null);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(frameworkError.getHeaders()).contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        }
        log.error("Erro inesperado no UserController", exception);
        return handleHttp(new InternalServerErrorHttpException());
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class,
            TypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidParameters(Exception exception) {
        return handleHttp(new BadRequestHttpException());
    }
}
