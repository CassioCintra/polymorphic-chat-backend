package io.cassio.polymorphic.chat.infrastructure.ai.cohere.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.cohere")
public record CohereProperties(
        String baseUrl,
        String apiKey,
        String model
) {}

