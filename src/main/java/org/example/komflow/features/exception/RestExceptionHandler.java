package org.example.komflow.features.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        // Simple error response body, can be enhanced later
        String errorDetails = ex.getMessage(); 
        // In a real app, you might have a structured error DTO
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
}
