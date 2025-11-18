package com.kompozith.komflow.configuration.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {
    private String secretKey;
    private long expirationMs;
    private long refreshExpirationMs;
    private String issuer;
    // Durée d'expiration du token d'accès en secondes
    private long accessTokenExpirationSeconds;
}
