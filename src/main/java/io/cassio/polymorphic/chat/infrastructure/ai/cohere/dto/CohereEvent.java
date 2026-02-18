package io.cassio.polymorphic.chat.infrastructure.ai.cohere.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CohereEvent(
        String type,
        Delta delta
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Delta(
            Message message,
            String finish_reason
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            Content content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            String text
    ) {}
}