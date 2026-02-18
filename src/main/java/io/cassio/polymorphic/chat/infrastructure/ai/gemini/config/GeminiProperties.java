package io.cassio.polymorphic.chat.infrastructure.ai.gemini.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.gemini")
public record GeminiProperties(
        String baseUrl,
        String apiKey,
        String model
) {}
