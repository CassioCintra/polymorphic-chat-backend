package io.cassio.polymorphic.chat.infrastructure.ai.cohere;


import io.cassio.polymorphic.chat.application.port.AiClient;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiType;
import io.cassio.polymorphic.chat.infrastructure.ai.cohere.config.CohereProperties;
import io.cassio.polymorphic.chat.infrastructure.ai.cohere.dto.CohereEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class CohereAiClient implements AiClient {

    private final WebClient cohereWebClient;
    private final String defaultModel;

    public CohereAiClient(CohereProperties properties) {
        this.defaultModel = properties.model();
        this.cohereWebClient = WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public AiType getType() { return AiType.COHERE; }

    @Override
    public Flux<String> chatStream(AiChatRequest request) {

        var body = Map.of(
                "stream", true,
                "model", defaultModel,
                "messages", request.messages().stream()
                        .map(message -> Map.of(
                                "role", message.role(),
                                "content", message.content()
                        ))
                        .toList()
        );

        return cohereWebClient.post()
                .uri("/v2/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<CohereEvent>>() {})
                .mapNotNull(ServerSentEvent::data)
                .filter(ev -> "content-delta".equals(ev.type()))
                .mapNotNull(ev -> ev.delta() != null
                        && ev.delta().message() != null
                        && ev.delta().message().content() != null
                        ? ev.delta().message().content().text()
                        : null
                )
                .onBackpressureLatest();
    }
}