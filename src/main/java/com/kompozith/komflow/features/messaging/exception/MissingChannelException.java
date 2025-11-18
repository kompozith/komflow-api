package com.kompozith.komflow.features.messaging.exception;

public class MissingChannelException extends RuntimeException {
    public MissingChannelException(String message) {
        super(message);
    }
}