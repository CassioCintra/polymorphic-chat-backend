package io.cassio.polymorphic.chat.application.service;

import io.cassio.polymorphic.chat.application.factory.AiClientFactory;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiClientFactory factory;

    public Flux<String> chatStream(AiType type, AiChatRequest request) {
        return factory.get(type).chatStream(request);
    }
}
