package com.inxbtwt.infra.exception.bucket;

public class BucketFileNotFoundException extends RuntimeException {
    public BucketFileNotFoundException(Throwable cause) {
        super("Foto não encontrada no bucket.", cause);
    }
}
