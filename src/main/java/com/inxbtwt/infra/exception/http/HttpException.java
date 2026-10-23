package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatus;

public abstract class HttpException extends RuntimeException {
    private final HttpStatus status;

    protected HttpException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
