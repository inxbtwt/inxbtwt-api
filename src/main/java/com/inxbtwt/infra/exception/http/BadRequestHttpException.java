package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatus;

public class BadRequestHttpException extends HttpException {
    public BadRequestHttpException() {
        this(HttpMessages.BAD_REQUEST);
    }

    public BadRequestHttpException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
