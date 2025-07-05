package com.kompozith.komflow.configuration.exception;

public class ObjectExistException extends RuntimeException {

    public ObjectExistException(String className, String fieldName, String fieldValue) {
        super(className + " already exists with " + fieldName + " " + fieldValue + ".");
    }

    public ObjectExistException(String message, Throwable cause) {
        super(message, cause);
    }
}