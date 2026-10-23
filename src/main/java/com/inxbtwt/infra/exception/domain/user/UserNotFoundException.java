package com.inxbtwt.infra.exception.domain.user;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
