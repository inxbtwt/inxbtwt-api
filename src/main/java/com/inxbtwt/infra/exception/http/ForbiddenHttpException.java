package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatus;

public class ForbiddenHttpException extends HttpException {

    public ForbiddenHttpException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
