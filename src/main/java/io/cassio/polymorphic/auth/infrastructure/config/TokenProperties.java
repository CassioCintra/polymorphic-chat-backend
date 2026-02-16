package io.cassio.polymorphic.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.token")
public record TokenProperties(
        Access access,
        Refresh refresh
){
    public record Access(Long expiration) {}
    public record Refresh(Long expiration) {}
}