package com.kompozith.komflow.configuration.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtConfigValidator {

    private final JwtConfig jwtConfig;

    @PostConstruct
    public void validateJwtConfiguration() {
        if (jwtConfig.getSecretKey() == null || jwtConfig.getSecretKey().isBlank()) {
            throw new IllegalStateException("JWT secret-key must be configured (jwt.secret-key)");
        }

        if (jwtConfig.getSecretKey().length() < 32) {
            throw new IllegalStateException("JWT secret-key must be at least 32 characters long");
        }

        if (jwtConfig.getExpirationMs() <= 0) {
            throw new IllegalStateException("JWT expiration-ms must be configured and positive");
        }

        if (jwtConfig.getRefreshExpirationMs() <= 0) {
            throw new IllegalStateException("JWT refresh-expiration-ms must be configured and positive");
        }

        if (jwtConfig.getRefreshExpirationMs() <= jwtConfig.getExpirationMs()) {
            throw new IllegalStateException("JWT refresh-expiration-ms must be longer than access token expiration");
        }

        log.info("JWT configuration validated: access={} ms, refresh={} ms, issuer={}",
                jwtConfig.getExpirationMs(),
                jwtConfig.getRefreshExpirationMs(),
                jwtConfig.getIssuer());
    }
}
