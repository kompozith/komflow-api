package com.kompozith.komflow.features.core.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Identifiants du compte administrateur initial (bootstrap), externalisés
 * depuis application.yml / variables d'environnement plutôt que codés en dur.
 */
@Component
@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminBootstrapProperties {
    private String email;
    private String password;
}
