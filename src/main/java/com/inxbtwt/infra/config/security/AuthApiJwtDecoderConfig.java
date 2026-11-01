package com.inxbtwt.infra.config.security;

import com.inxbtwt.infra.properties.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class AuthApiJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        var decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwksUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(properties.audience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        OAuth2TokenValidator<Jwt> accessTokenValidator = jwt -> "access".equals(
                jwt.getClaimAsString("token_type"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                audienceValidator,
                accessTokenValidator
        ));

        return decoder;
    }
}
