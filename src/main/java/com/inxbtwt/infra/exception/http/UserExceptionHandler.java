package com.inxbtwt.infra.exception.http;

import com.inxbtwt.infra.controller.user.UserController;
import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.infra.exception.bucket.BucketStorageException;
import com.inxbtwt.infra.exception.bucket.BucketFileNotFoundException;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import lombok.extern.slf4j.Slf4j;
import com.inxbtwt.infra.exception.domain.DomainException;
import com.inxbtwt.infra.exception.domain.user.UserAlreadyExistException;
import com.inxbtwt.infra.exception.domain.user.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException exception) {
        return ApiControllerUtils.unauthorized(exception.getMessage());
    }

    @ExceptionHandler(BucketFileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBucketFileNotFound(BucketFileNotFoundException exception) {
        return ApiControllerUtils.notFound(exception.getMessage());
    }

    @ExceptionHandler(BucketStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleBucketStorage(BucketStorageException exception) {
        log.error("Falha no armazenamento da foto do usuário", exception);
        return ApiControllerUtils.serviceUnavailable();
    }

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttp(HttpException exception) {
        return ApiControllerUtils.error(exception.getStatus(), exception.getMessage());
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
            return ApiControllerUtils.error(frameworkError.getStatusCode(), null, frameworkError.getHeaders());
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
