package com.inxbtwt.infra.exception.http;

import org.springframework.http.HttpStatus;

public class ConflictHttpException extends HttpException {
    public ConflictHttpException() {
        this(HttpMessages.CONFLICT);
    }

    public ConflictHttpException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
