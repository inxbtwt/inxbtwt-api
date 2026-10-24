package com.inxbtwt.infra.exception.http;
import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.infra.controller.auth.AuthController;
import com.inxbtwt.infra.controller.auth.MeController;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes = {AuthController.class, MeController.class})
public class AuthExceptionHandler extends UserExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException exception) {
        return ApiControllerUtils.unauthorized(exception.getMessage());
    }
}
