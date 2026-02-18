package io.cassio.polymorphic.chat.application.factory;

import io.cassio.polymorphic.chat.application.port.AiClient;
import io.cassio.polymorphic.chat.domain.AiType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AiClientFactory {

    private final Map<AiType, AiClient> clients;

    public AiClientFactory(List<AiClient> clients) {
        this.clients = clients.stream()
                .collect(Collectors.toMap(
                        AiClient::getType,
                        Function.identity(),
                        (a, b) -> a,
                        () -> new EnumMap<>(AiType.class)
                ));
    }

    public AiClient get(AiType type) {
        return clients.get(type);
    }
}
