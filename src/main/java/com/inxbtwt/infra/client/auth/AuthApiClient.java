package com.inxbtwt.infra.client.auth;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.jwt.JwtGenerateResponse;
import com.inxbtwt.domain.dto.jwt.LoginRequest;
import com.inxbtwt.domain.dto.jwt.RefreshTokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-api", url = "${app.auth-api.url}")
public interface AuthApiClient {

    @PostMapping("/auth")
    ApiResponse<JwtGenerateResponse> authenticate(@RequestBody LoginRequest request);

    @PostMapping("/auth/refresh")
    ApiResponse<JwtGenerateResponse> refresh(@RequestBody RefreshTokenRequest request);

    @PostMapping("/auth/logout")
    ApiResponse<Void> logout(@RequestBody RefreshTokenRequest request);
}
