package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatus;

public class NotFoundHttpException extends HttpException {
    public NotFoundHttpException() {
        this(HttpMessages.NOT_FOUND);
    }

    public NotFoundHttpException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
