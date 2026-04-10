package com.kompozith.komflow.features.messaging.service;

/**
 * Exception levée lorsque l'envoi d'un message WhatsApp échoue.
 */
public class WhatsAppSendException extends RuntimeException {

    private final int httpStatus;
    private final String apiErrorMessage;

    public WhatsAppSendException(String message, int httpStatus, String apiErrorMessage) {
        super(message);
        this.httpStatus = httpStatus;
        this.apiErrorMessage = apiErrorMessage;
    }

    public WhatsAppSendException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = -1;
        this.apiErrorMessage = null;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getApiErrorMessage() {
        return apiErrorMessage;
    }
}
