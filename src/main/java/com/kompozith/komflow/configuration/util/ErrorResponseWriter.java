package com.kompozith.komflow.configuration.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {
    private final ObjectMapper objectMapper;

    public void writeErrorResponse(HttpServletResponse response,
                                   HttpStatus status,
                                   RuntimeException ex) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status.value());
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorResponse(status.getReasonPhrase().toUpperCase(), ex.getMessage())
        ));
    }
}