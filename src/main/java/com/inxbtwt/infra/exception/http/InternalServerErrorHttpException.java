package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatus;

public class InternalServerErrorHttpException extends HttpException {
    public InternalServerErrorHttpException() {
        this(HttpMessages.INTERNAL_SERVER_ERROR);
    }

    public InternalServerErrorHttpException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
