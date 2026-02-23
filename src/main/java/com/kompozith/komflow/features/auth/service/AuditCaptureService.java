package com.kompozith.komflow.features.auth.service;

import com.kompozith.komflow.features.auth.entity.AuditLog;
import com.kompozith.komflow.features.auth.repository.AuditLogRepository;
import com.kompozith.komflow.features.personnel.entity.User;
import com.kompozith.komflow.features.personnel.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditCaptureService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void capture(HttpServletRequest request, String username, int status, long durationMs, Throwable failure) {
        try {
            String uri = request.getRequestURI();
            String action = resolveAction(request.getMethod(), uri);
            String objectType = resolveObjectType(uri);
            String objectId = resolveObjectId(uri);

            AuditLog logEntry = new AuditLog();
            logEntry.setAction(action);
            logEntry.setObjectType(objectType);
            logEntry.setObjectId(objectId != null ? objectId : "-");
            logEntry.setConnectedUserId(resolveConnectedUserId(username).orElse(null));
            logEntry.setIpAddress(resolveClientIp(request));
            logEntry.setUserAgent(request.getHeader("User-Agent"));
            logEntry.setUserLocation(null);
            logEntry.setChannel(resolveChannel(uri));
            logEntry.setDetails(buildDetails(request.getMethod(), uri, status, durationMs, failure));

            auditLogRepository.save(logEntry);
        } catch (Exception ex) {
            // Never fail business flow because of audit persistence issue.
            log.warn("Failed to persist audit log: {}", ex.getMessage());
        }
    }

    private Optional<String> resolveConnectedUserId(String username) {
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim()).map(User::getId).map(String::valueOf);
    }

    private String resolveAction(String method, String uri) {
        String normalizedUri = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
        if (normalizedUri.contains("/auth/login")) return "LOGIN";
        if (normalizedUri.contains("/auth/logout")) return "LOGOUT";
        if (normalizedUri.contains("/files/upload")) return "FILE_UPLOAD";
        if (normalizedUri.contains("/files/") && normalizedUri.endsWith("/download")) return "FILE_DOWNLOAD";
        if (normalizedUri.contains("/campaigns/") && normalizedUri.endsWith("/submit")) return "CAMPAIGN_SEND";
        if (normalizedUri.contains("/campaigns/") && normalizedUri.contains("/cancel")) return "CAMPAIGN_CANCEL";

        return switch ((method == null ? "" : method).toUpperCase(Locale.ROOT)) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            case "GET" -> "READ";
            default -> "OTHER";
        };
    }

    private String resolveObjectType(String uri) {
        String[] segments = splitPath(uri);
        if (segments.length == 0) {
            return "SYSTEM";
        }

        int idx = 0;
        if ("api".equalsIgnoreCase(segments[idx])) idx++;
        if (idx < segments.length && segments[idx].matches("v\\d+")) idx++;
        if (idx >= segments.length) return "SYSTEM";

        String resource = segments[idx].toUpperCase(Locale.ROOT);
        if (resource.endsWith("S") && resource.length() > 1) {
            resource = resource.substring(0, resource.length() - 1);
        }
        return resource;
    }

    private String resolveObjectId(String uri) {
        String[] segments = splitPath(uri);
        return Arrays.stream(segments)
                .filter(s -> s.matches("\\d+"))
                .findFirst()
                .orElse(null);
    }

    private String resolveChannel(String uri) {
        String normalizedUri = uri == null ? "" : uri.toLowerCase(Locale.ROOT);
        if (normalizedUri.contains("/messages")) return "MESSAGE";
        if (normalizedUri.contains("/campaigns")) return "CAMPAIGN";
        if (normalizedUri.contains("/files")) return "FILE";
        if (normalizedUri.contains("/auth")) return "AUTH";
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildDetails(String method, String uri, int status, long durationMs, Throwable failure) {
        String base = String.format("%s %s | status=%d | durationMs=%d", method, uri, status, durationMs);
        if (failure == null) {
            return base;
        }
        String error = failure.getClass().getSimpleName() + ": " + (failure.getMessage() == null ? "" : failure.getMessage());
        return base + " | error=" + error;
    }

    private String[] splitPath(String uri) {
        if (uri == null || uri.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(uri.split("/"))
                .filter(part -> part != null && !part.isBlank())
                .toArray(String[]::new);
    }
}
