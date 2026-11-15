package com.inxbtwt.domain.service.auth;
import com.inxbtwt.domain.dto.jwt.*;

public interface AuthService {
    JwtGenerateResponse authenticate(LoginRequest request);
    JwtGenerateResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
}
