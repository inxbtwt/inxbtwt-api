package com.inxbtwt.domain.util;

import com.inxbtwt.domain.dto.jwt.JwtGenerateResponse;
import com.inxbtwt.infra.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public final class JwtUtils {
    private final JwtProperties properties;
    private final SecretKey key;
    private final JwtParser parser;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        requirePositive(properties.accessTokenExpiration());
        requirePositive(properties.refreshTokenExpiration());
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.parser = Jwts.parser().verifyWith(key).requireIssuer(properties.issuer())
                .sig().clear().add(Jwts.SIG.HS256).and().build();
    }

    public JwtGenerateResponse generate(UUID userId, Collection<String> roles) {
        Objects.requireNonNull(userId, "O usuário é obrigatório.");
        var authorities = List.copyOf(Objects.requireNonNull(roles, "As roles são obrigatórias."));
        if (authorities.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("As roles não podem estar vazias.");
        }
        var now = Instant.now();
        var accessToken = createToken(userId, authorities, "access", now, properties.accessTokenExpiration());
        var refreshToken = createToken(userId, List.of(), "refresh", now, properties.refreshTokenExpiration());
        return new JwtGenerateResponse(accessToken, refreshToken, "Bearer",
                properties.accessTokenExpiration().toSeconds());
    }

    public boolean validate(String token) {
        return validateType(token, "access");
    }

    public boolean validateRefreshToken(String token) {
        return validateType(token, "refresh");
    }

    public Claims decode(String token) {
        var claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getIssuedAt() == null
                || claims.getSubject() == null || claims.getId() == null) {
            throw new JwtException("Token sem os campos obrigatórios.");
        }
        UUID.fromString(claims.getSubject());
        var type = claims.get("token_type", String.class);
        if (!"access".equals(type) && !"refresh".equals(type)) {
            throw new JwtException("Tipo de token inválido.");
        }
        return claims;
    }

    private boolean validateType(String token, String expectedType) {
        try {
            return expectedType.equals(decode(token).get("token_type", String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private String createToken(UUID userId, List<String> roles, String type, Instant now, Duration expiration) {
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .claim("token_type", type)
                .claim("roles", roles)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private static void requirePositive(Duration duration) {
        if (duration == null || duration.toSeconds() < 1) {
            throw new IllegalArgumentException("A validade do token deve ser de pelo menos um segundo.");
        }
    }
}
