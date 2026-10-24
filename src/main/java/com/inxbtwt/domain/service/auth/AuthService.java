package com.inxbtwt.domain.service.auth;
import com.inxbtwt.domain.dto.jwt.*;
import com.inxbtwt.domain.dto.user.http.UserResponse;
import java.util.UUID;
public interface AuthService {
    JwtGenerateResponse authenticate(LoginRequest request);
    JwtGenerateResponse refresh(RefreshTokenRequest request);
    UserResponse me(UUID userId);
}
