package com.inxbtwt.infra.exception.domain;

public  class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
