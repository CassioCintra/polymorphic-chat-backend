package io.cassio.polymorphic.chat.application.port;

import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiType;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiClient {

    AiType getType();

    Flux<String> chatStream(AiChatRequest request);

}
