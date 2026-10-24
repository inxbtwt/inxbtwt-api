package com.inxbtwt.infra.config.security;

import com.inxbtwt.domain.util.JwtUtils;
import com.inxbtwt.db.repository.UserDataRepository;
import io.jsonwebtoken.JwtException;
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
        return List.of("/auth", "/auth/refresh", "/auth/csrf").contains(request.getServletPath());
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
                if (!"access".equals(claims.get("token_type", String.class))) throw new JwtException("Access token obrigatório.");
                var userId = UUID.fromString(claims.getSubject());

                if (users.findByUserId(userId).isEmpty()) throw new JwtException("Usuário inexistente.");

                var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
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
