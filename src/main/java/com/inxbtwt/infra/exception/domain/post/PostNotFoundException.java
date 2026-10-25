package com.inxbtwt.infra.exception.domain.post;

import com.inxbtwt.infra.exception.domain.DomainException;

public class PostNotFoundException extends DomainException {

    public PostNotFoundException(String message) {
        super(message);
    }
}
