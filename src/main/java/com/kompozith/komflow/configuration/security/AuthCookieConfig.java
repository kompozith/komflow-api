package com.kompozith.komflow.configuration.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.cookie")
@Getter
@Setter
public class AuthCookieConfig {
    private String name;
    private String path;
    private String sameSite;
    private boolean secure;
    private boolean httpOnly;
}
