package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.util.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String authorities = auth != null ? auth.getAuthorities().toString() : "none";

        log.warn("Access denied for user: {} with authorities: {} on path: {}",
                username, authorities, request.getServletPath());

        errorResponseWriter.writeErrorResponse(
                response,
                HttpStatus.FORBIDDEN,
                new com.kompozith.komflow.exception.AccessDeniedException("You don't have permission to access this resource")
        );
    }
}
