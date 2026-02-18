package io.cassio.polymorphic.chat.infrastructure.websocket;

import io.cassio.polymorphic.chat.application.service.AiChatService;
import io.cassio.polymorphic.chat.application.service.ChatRoomService;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiType;
import io.cassio.polymorphic.chat.infrastructure.websocket.dto.WebSocketRequest;
import io.cassio.polymorphic.chat.infrastructure.websocket.dto.WebSocketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock ChatRoomService roomService;
    @Mock AiChatService chatService;
    @Mock WebSocketJsonConverter json;
    @Mock WebSocketSession session;
    private ChatWebSocketHandler handler;
    private List<String> sent;

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler(roomService, chatService, json);
        sent = new CopyOnWriteArrayList<>();
    }

    @Test
    void shouldSendRoomCreatedThenDeltasAndDone() {
        enableOutboundCapture();

        var userUuid = UUID.randomUUID();
        var sessionId = "s-1";
        givenAuthenticated(userUuid);
        givenSessionId(sessionId);

        givenRoom(userUuid, sessionId);
        givenInboundJson();
        givenDecodedRequest(new WebSocketRequest("user_message", AiType.COHERE, "oi", null));
        givenAiStream();

        StepVerifier.create(handler.handle(session)).verifyComplete();

        assertThat(sent).containsExactly(
                "room_created:room-1:" + userUuid,
                "assistant_delta:room-1:Olá",
                "assistant_delta:room-1: mundo",
                "assistant_done:room-1"
        );

        verify(roomService).closeRoom("room-1");
    }

    @Test
    void shouldFailWhenUnauthenticated() {
        givenUnauthenticated();

        StepVerifier.create(handler.handle(session))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalStateException.class);
                    assertThat(ex.getMessage()).contains("Unauthenticated websocket");
                })
                .verify();

        verifyNoInteractions(roomService, chatService, json);
        assertThat(sent).isEmpty();
    }

    private void enableOutboundCapture() {
        stubTextMessageEcho();
        stubSendCapture(sent);
        stubEncodeDeterministic();
    }

    private void stubTextMessageEcho() {
        when(session.textMessage(anyString())).thenAnswer(inv -> {
            var text = inv.getArgument(0, String.class);
            var m = mock(WebSocketMessage.class);
            when(m.getPayloadAsText()).thenReturn(text);
            return m;
        });
    }

    private void stubSendCapture(List<String> sink) {
        when(session.send(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            var pub = (org.reactivestreams.Publisher<WebSocketMessage>) inv.getArgument(0);

            return Flux.from(pub)
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::add)
                    .then();
        });
    }

    private void stubEncodeDeterministic() {
        when(json.encodeSync(any(WebSocketResponse.class))).thenAnswer(inv -> encode(inv.getArgument(0)));
    }

    private static String encode(WebSocketResponse<?> r) {
        return switch (r.type()) {
            case "room_created" -> {
                var d = (WebSocketResponse.RoomCreated) r.data();
                yield "room_created:" + d.roomId() + ":" + d.userUuid();
            }
            case "assistant_delta" -> {
                var d = (WebSocketResponse.AssistantDelta) r.data();
                yield "assistant_delta:" + d.roomId() + ":" + d.delta();
            }
            case "assistant_done" -> {
                var d = (WebSocketResponse.AssistantDone) r.data();
                yield "assistant_done:" + d.roomId();
            }
            case "error" -> {
                var d = (WebSocketResponse.Error) r.data();
                yield "error:" + d.code() + ":" + d.message();
            }
            default -> r.type() + ":?";
        };
    }

    private void givenAuthenticated(UUID userUuid) {
        var auth = new UsernamePasswordAuthenticationToken(userUuid.toString(), "N/A", List.of());
        var handshake = mock(HandshakeInfo.class);
        when(session.getHandshakeInfo()).thenReturn(handshake);
        when(handshake.getPrincipal()).thenReturn(Mono.just(auth));
    }

    private void givenUnauthenticated() {
        var handshake = mock(HandshakeInfo.class);
        when(session.getHandshakeInfo()).thenReturn(handshake);
        when(handshake.getPrincipal()).thenReturn(Mono.empty());
    }

    private void givenSessionId(String sessionId) {
        when(session.getId()).thenReturn(sessionId);
    }

    private void givenRoom(UUID userUuid, String sessionId) {
        var room = new ChatRoomService.Room("room-1", userUuid, sessionId, Instant.now());
        when(roomService.createRoom(userUuid, sessionId)).thenReturn(room);
    }

    private void givenInboundJson() {
        var inboundMsg = mock(WebSocketMessage.class);
        when(inboundMsg.getPayloadAsText()).thenReturn("{json}");
        when(session.receive()).thenReturn(Flux.just(inboundMsg));
    }

    private void givenDecodedRequest(WebSocketRequest req) {
        when(json.decodeSync("{json}", WebSocketRequest.class)).thenReturn(req);
    }

    private void givenAiStream() {
        when(chatService.chatStream(eq(AiType.COHERE), any(AiChatRequest.class)))
                .thenReturn(Flux.just("Olá", " mundo"));
    }
}