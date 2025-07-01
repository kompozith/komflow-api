package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.configuration.exception.JwtAuthenticationException;
import com.kompozith.komflow.configuration.util.ErrorResponseWriter;
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

        errorResponseWriter.writeErrorResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                new JwtAuthenticationException(authException.getMessage())
        );
    }
}