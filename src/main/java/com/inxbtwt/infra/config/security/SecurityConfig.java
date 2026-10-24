package com.inxbtwt.infra.config.security;

import com.inxbtwt.infra.properties.SecurityRoutesProperties;
import com.inxbtwt.domain.util.JwtUtils;
import com.inxbtwt.db.repository.UserDataRepository;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.http.HttpStatus;
import java.util.Map;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityRoutesProperties securityRoutes;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtils jwt, UserDataRepository users,
                                          SecurityResponseWriter writer) throws Exception {
        var csrfRepository = new CookieCsrfTokenRepository();
        csrfRepository.setCookieName("__Host-XSRF-TOKEN");
        csrfRepository.setCookieCustomizer(cookie -> cookie.secure(true).httpOnly(true).sameSite("Strict").path("/"));

        http
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository)
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writer.write(response, HttpStatus.UNAUTHORIZED, "Autenticação obrigatória."))
                        .accessDeniedHandler((request, response, exception) ->
                                writer.write(response, HttpStatus.FORBIDDEN, "Acesso negado.")))
                .addFilterBefore(new JwtAuthenticationFilter(jwt, users, writer), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> {
            for (var route : securityRoutes.routes()) {
                var rule = authorize.requestMatchers(HttpMethod.valueOf(route.method()), route.path());

                switch (route.access()) {
                    case PERMIT_ALL -> rule.permitAll();
                    case AUTHENTICATED -> rule.authenticated();
                    case ROLES -> rule.hasAnyRole(route.roles().toArray(String[]::new));
                }
            }

            authorize.anyRequest().denyAll();
        });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder("pbkdf2",
                Map.of("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));
    }
}
