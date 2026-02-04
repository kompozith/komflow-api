package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.exception.JwtAuthenticationException;
import com.kompozith.komflow.util.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        JwtAuthenticationException jwtError = (JwtAuthenticationException) request.getAttribute("jwtError");
        Exception authError = (Exception) request.getAttribute("authError");

        if (jwtError != null) {
            errorResponseWriter.writeErrorResponse(response, HttpStatus.UNAUTHORIZED, jwtError);
            return;
        }

        if (authError != null) {
            errorResponseWriter.writeErrorResponse(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    new JwtAuthenticationException("Authentication failed: " + authError.getMessage())
            );
            return;
        }

        errorResponseWriter.writeErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                new JwtAuthenticationException(determineGenericMessage(authException))
        );
    }

    private String determineGenericMessage(AuthenticationException authException) {
        String message = authException.getMessage();
        if (message == null) {
            return "Authentication failed. Please check your credentials and try again.";
        }
        if (message.contains("Full authentication is required")) {
            return "You need to login first to access this resource";
        }
        if (message.contains("Access is denied")) {
            return "You don't have permission to access this resource";
        }
        return "Authentication failed. Please check your credentials and try again.";
    }
}
