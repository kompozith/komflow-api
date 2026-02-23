package com.kompozith.komflow.configuration.security;

import com.kompozith.komflow.features.auth.service.AuditCaptureService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuditTrailFilter extends OncePerRequestFilter {

    private final AuditCaptureService auditCaptureService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || uri.contains("/swagger-ui")
                || uri.contains("/v3/api-docs")
                || uri.endsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        Throwable failure = null;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            failure = ex;
            throw ex;
        } catch (Error err) {
            failure = err;
            throw err;
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            String username = resolveUsername();
            auditCaptureService.capture(request, username, response.getStatus(), durationMs, failure);
        }
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return authentication.getName();
    }
}
