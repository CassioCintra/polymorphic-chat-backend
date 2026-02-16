package io.cassio.polymorphic.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cookies")
public record CookieProperties(
        boolean secure,
        String sameSite
) {}
