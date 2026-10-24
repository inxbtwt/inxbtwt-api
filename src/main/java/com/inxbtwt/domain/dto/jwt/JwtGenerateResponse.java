package com.inxbtwt.domain.dto.jwt;

public record JwtGenerateResponse(
        String token,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
