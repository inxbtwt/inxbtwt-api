package com.inxbtwt.infra.config.security;

import com.inxbtwt.domain.dto.jwt.JwtGenerateResponse;
import com.inxbtwt.infra.properties.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookies {

    public static final String ACCESS_TOKEN = "__Host-AccessToken";
    public static final String REFRESH_TOKEN = "__Secure-RefreshToken";
    private static final String REFRESH_PATH = "/auth";
    private static final String LEGACY_REFRESH_PATH = "/auth/refresh";
    private final JwtProperties properties;

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true).secure(true).sameSite("Strict").path("/")
                .maxAge(0).build().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true).secure(true).sameSite("Strict").path(REFRESH_PATH)
                .maxAge(0).build().toString());
        clearLegacyRefreshCookie(response);
    }

    public void write(HttpServletResponse response, JwtGenerateResponse tokens) {
        clearLegacyRefreshCookie(response);
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(ACCESS_TOKEN, tokens.token())
                .httpOnly(true).secure(true).sameSite("Strict").path("/")
                .maxAge(properties.accessTokenExpiration()).build().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_TOKEN, tokens.refreshToken())
                .httpOnly(true).secure(true).sameSite("Strict").path(REFRESH_PATH)
                .maxAge(properties.refreshTokenExpiration()).build().toString());
    }

    private void clearLegacyRefreshCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true).secure(true).sameSite("Strict").path(LEGACY_REFRESH_PATH)
                .maxAge(0).build().toString());
    }
}
