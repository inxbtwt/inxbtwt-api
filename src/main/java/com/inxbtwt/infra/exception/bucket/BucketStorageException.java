package com.inxbtwt.infra.exception.bucket;

public class BucketStorageException extends RuntimeException {
    public BucketStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
