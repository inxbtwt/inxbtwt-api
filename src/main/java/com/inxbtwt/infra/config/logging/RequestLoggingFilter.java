package com.inxbtwt.infra.config.logging;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var path = request.getRequestURI();
        return path.contains("/actuator") || path.contains("/swagger-ui") || path.contains("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            var elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            var status = response.getStatus();

            if (status >= 500) {
                log.error("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
            } else if (status >= 400) {
                log.warn("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
            } else {
                log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
            }
        }
    }
}
