package com.inxbtwt.infra.config.security;

import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

@Component
public class SecurityResponseWriter {
    private final ObjectMapper mapper;

    public SecurityResponseWriter(ObjectMapper mapper) { this.mapper = mapper; }

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), ApiControllerUtils.error(status, message).getBody());
    }
}
