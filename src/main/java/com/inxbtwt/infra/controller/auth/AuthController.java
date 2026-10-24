package com.inxbtwt.infra.controller.auth;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.jwt.AuthResponse;
import com.inxbtwt.domain.dto.jwt.CsrfResponse;
import com.inxbtwt.domain.dto.jwt.LoginRequest;
import com.inxbtwt.domain.dto.jwt.RefreshTokenRequest;
import com.inxbtwt.domain.service.auth.AuthService;
import com.inxbtwt.infra.config.security.AuthCookies;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthCookies cookies;

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<CsrfResponse>> csrf(CsrfToken csrfToken) {
        return ApiControllerUtils.ok("Token CSRF gerado.",
                new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(@Valid @RequestBody LoginRequest request,
                                                                  HttpServletResponse response) {
        var tokens = authService.authenticate(request);
        cookies.write(response, tokens);
        return ApiControllerUtils.ok("Autenticação realizada com sucesso.", new AuthResponse(tokens.expiresIn()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = AuthCookies.REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response) {
        var tokens = authService.refresh(new RefreshTokenRequest(refreshToken));
        cookies.write(response, tokens);
        return ApiControllerUtils.ok("Token renovado com sucesso.", new AuthResponse(tokens.expiresIn()));
    }
}
