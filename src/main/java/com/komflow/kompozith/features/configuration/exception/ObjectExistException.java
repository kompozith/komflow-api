package com.komflow.kompozith.features.configuration.exception;

public class ObjectExistException extends RuntimeException {

    public ObjectExistException(String message) {
        super(message);
    }

    public ObjectExistException(String message, Throwable cause) {
        super(message, cause);
    }
}