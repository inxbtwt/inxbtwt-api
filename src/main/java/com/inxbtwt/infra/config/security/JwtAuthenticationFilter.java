package com.inxbtwt.infra.config.security;

import com.inxbtwt.domain.util.JwtUtils;
import com.inxbtwt.db.repository.UserDataRepository;
import org.springframework.security.oauth2.jwt.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwt;
    private final UserDataRepository users;
    private final SecurityResponseWriter writer;

    public JwtAuthenticationFilter(JwtUtils jwt, UserDataRepository users, SecurityResponseWriter writer) {
        this.jwt = jwt; this.users = users; this.writer = writer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return List.of("/auth", "/auth/refresh", "/auth/csrf", "/auth/logout")
                .contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (AuthCookies.ACCESS_TOKEN.equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null) {
            try {
                var claims = jwt.decode(token);
                if (!"access".equals(claims.getClaimAsString("token_type"))) {
                    throw new JwtException("Access token obrigatório.");
                }
                var userId = UUID.fromString(claims.getSubject());
                var roles = claims.getClaimAsStringList("roles");
                if (roles == null || roles.isEmpty()) {
                    throw new JwtException("O token não possui permissões.");
                }

                if (users.findByUserId(userId).isEmpty()) throw new JwtException("Usuário inexistente.");

                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
                writer.write(response, HttpStatus.UNAUTHORIZED, "Token inválido ou expirado.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
