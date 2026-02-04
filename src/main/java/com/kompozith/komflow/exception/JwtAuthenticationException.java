package com.kompozith.komflow.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {

    private final JwtErrorType errorType;

    public JwtAuthenticationException(String message) {
        super(message);
        this.errorType = JwtErrorType.INVALID_CLAIMS;
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = JwtErrorType.INVALID_CLAIMS;
    }

    public JwtAuthenticationException(String message, JwtErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public JwtAuthenticationException(String message, Throwable cause, JwtErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    public enum JwtErrorType {
        EXPIRED("JWT token has expired"),
        INVALID_SIGNATURE("JWT token signature is invalid"),
        MALFORMED("JWT token is malformed"),
        UNSUPPORTED("JWT token is unsupported"),
        INVALID_CLAIMS("JWT token has invalid claims"),
        MISSING("JWT token is missing"),
        INVALID_FORMAT("JWT token format is invalid");

        private final String description;

        JwtErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
