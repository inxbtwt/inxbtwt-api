package com.inxbtwt.infra.exception.domain.user;

public class UserAlreadyExistException extends UserException {
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
