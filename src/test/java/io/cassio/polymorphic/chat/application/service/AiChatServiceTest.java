package io.cassio.polymorphic.chat.application.service;


import io.cassio.polymorphic.chat.application.factory.AiClientFactory;
import io.cassio.polymorphic.chat.application.port.AiClient;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock private AiClientFactory factory;
    @Mock private AiClient client;
    @InjectMocks private AiChatService service;

    @Test
    void shouldDelegateToClientChatStream() {
        AiType type = AiType.COHERE;
        AiChatRequest request = mock(AiChatRequest.class);
        Flux<String> expected = Flux.just("a", "b", "c");
        when(factory.get(type)).thenReturn(client);
        when(client.chatStream(request)).thenReturn(expected);

        StepVerifier.create(service.chatStream(type, request))
                .expectNext("a", "b", "c")
                .verifyComplete();

        verify(factory).get(type);

        ArgumentCaptor<AiChatRequest> captor = ArgumentCaptor.forClass(AiChatRequest.class);
        verify(client).chatStream(captor.capture());
        assertThat(captor.getValue()).isSameAs(request);

        verifyNoMoreInteractions(factory, client);
    }

    @Test
    void shouldPropagateErrorFromClient() {
        AiType type = AiType.COHERE;
        AiChatRequest request = mock(AiChatRequest.class);
        RuntimeException error = new RuntimeException("boom");
        when(factory.get(type)).thenReturn(client);
        when(client.chatStream(request)).thenReturn(Flux.error(error));

        StepVerifier.create(service.chatStream(type, request))
                .expectErrorSatisfies(e -> assertThat(e).isSameAs(error))
                .verify();

        verify(factory).get(type);
        verify(client).chatStream(request);
        verifyNoMoreInteractions(factory, client);
    }
}