package com.inxbtwt.infra.exception.domain.user;

import com.inxbtwt.infra.exception.domain.DomainException;

public class UserException extends DomainException {
    public UserException(String message) {
        super(message);
    }
}
