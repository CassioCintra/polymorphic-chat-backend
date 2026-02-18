package io.cassio.polymorphic.chat.infrastructure.ai.gemini;

import io.cassio.polymorphic.chat.application.port.AiClient;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiType;
import io.cassio.polymorphic.chat.infrastructure.ai.gemini.config.GeminiProperties;
import io.cassio.polymorphic.chat.infrastructure.ai.gemini.dto.GeminiEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class GeminiAiClient implements AiClient {

    private final WebClient geminiWebClient;
    private final String defaultModel;

    public GeminiAiClient(GeminiProperties properties) {
        this.defaultModel = properties.model();
        this.geminiWebClient = WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-goog-api-key", properties.apiKey())
                .build();
    }

    @Override
    public AiType getType() { return AiType.GEMINI; }

    @Override
    public Flux<String> chatStream(AiChatRequest request) {
        var contents = request.messages().stream()
                .map(message -> Map.of(
                        "role", message.role(),
                        "parts", List.of(Map.of("text", message.content()))
                ))
                .toList();

        var body = Map.of("contents", contents);

        return geminiWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/{model}:streamGenerateContent")
                        .queryParam("alt", "sse")
                        .build(defaultModel)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<GeminiEvent>>() {})
                .mapNotNull(ServerSentEvent::data)
                .mapNotNull(ev -> ev.candidates() != null
                    && ev.candidates().getFirst().content() != null
                    && ev.candidates().getFirst().content().parts().getFirst() != null
                    ? ev.candidates().getFirst().content().parts().getFirst().text()
                    : null
                )
                .onBackpressureLatest();
    }

}