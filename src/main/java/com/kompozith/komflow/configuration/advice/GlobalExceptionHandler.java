package com.kompozith.komflow.configuration.advice;

import com.kompozith.komflow.exception.AccessDeniedException;
import com.kompozith.komflow.exception.InvalidCredentialsException;
import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.util.ErrorResponse;
import com.kompozith.komflow.util.SimpleResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Catcher et gere toutes les erreur de type bad request, cas specifique du validateur des donnes de requete.
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SimpleResponse<Map<String, String>> handleInvalidArgumentException(MethodArgumentNotValidException ex){
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            log.warn("Validation error field='{}' message='{}' codes='{}' object='{}'",
                error.getField(),
                error.getDefaultMessage(),
                String.join(",", error.getCodes() != null ? error.getCodes() : new String[0]),
                error.getObjectName());
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        return new SimpleResponse<>("INVALID_DATA", errorMap);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public SimpleResponse<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex){
        Map<String, String> errorMap = new HashMap<>();
        String message = ex.getMessage() != null ? ex.getMessage() : "INVALID_DATA";
        errorMap.put("personId", message);
        errorMap.put("person", message);
        return new SimpleResponse<>("INVALID_DATA", errorMap);
    }

    // Gestionnaire pour les erreurs 405 Method Not Allowed
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleMethodNotAllowedException(HttpRequestMethodNotSupportedException ex) {
        return new ErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage());
    }

    //Handle 404 not found exception
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoResourceFoundException.class, EntityNotFoundException.class, ObjectNotFoundException.class})
    public ErrorResponse handlerResourceFoundException(RuntimeException exception){
        return new ErrorResponse("RESOURCE_NOT_FOUND", exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException exception) {
        return new ErrorResponse("INVALID_CREDENTIALS", exception.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})
    public ErrorResponse handleAccessDeniedException(RuntimeException exception) {
        return new ErrorResponse("ACCESS_DENIED", "You are not authorized to access this resource");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ObjectExistException.class)
    public ErrorResponse handleObjectExistException(ObjectExistException exception) {
        return new ErrorResponse("CONFLICT", exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    public ErrorResponse handleNullPointerException(NullPointerException exception) {
        return new ErrorResponse("INTERNAL_SERVER_ERROR", exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = "DATA_INTEGRITY_VIOLATION";

        // Extraire la valeur et le champ dont la contrainte d'integrite est violée, du message d'erreur
        String violationInfo = extractErrorMessage(ex.getRootCause().getMessage());

        return new ErrorResponse(errorMessage, String.format(violationInfo));
    }

    // Méthode pour extraire la partie spécifique du message d'erreur
    private String extractErrorMessage(String errorMessage) {
        // Utiliser une expression régulière pour rechercher la partie spécifique du message d'erreur
        Pattern pattern = Pattern.compile("Key \\((.*?)\\) already exists.");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "Erreur interne du serveur.";
    }

}
