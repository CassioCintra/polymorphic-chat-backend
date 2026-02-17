package io.cassio.polymorphic.chat.infrastructure.websocket;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

@Component
public class WebSocketJsonConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<String> encode(Object value) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(value));
    }

    public String encodeSync(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalStateException("JSON serialization failed", e);
        }
    }

    public <T> Mono<T> decode(String json, Class<T> type) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, type));
    }

    public <T> T decodeSync(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON payload", e);
        }
    }
}

