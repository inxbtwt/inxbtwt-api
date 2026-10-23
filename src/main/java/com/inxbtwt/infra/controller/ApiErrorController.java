package com.inxbtwt.infra.controller;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.infra.exception.http.HttpMessages;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Handles errors raised before a controller is selected, including unknown routes. */
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Void>> error(HttpServletRequest request) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = code instanceof Integer value ? HttpStatus.resolve(value) : null;
        if (status == null || !status.isError()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(ApiResponse.<Void>builder()
                .message(HttpMessages.forStatus(status))
                .status(StatusCode.ERROR)
                .build());
    }
}
