package com.inxbtwt.domain.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class JwtUtils {
    private final JwtDecoder decoder;

    public Jwt decode(String token) {
        return decoder.decode(token);
    }
}
