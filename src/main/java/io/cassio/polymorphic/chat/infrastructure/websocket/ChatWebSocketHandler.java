package io.cassio.polymorphic.chat.infrastructure.websocket;

import io.cassio.polymorphic.chat.application.service.AiChatService;
import io.cassio.polymorphic.chat.application.service.ChatRoomService;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.infrastructure.websocket.dto.WebSocketRequest;
import io.cassio.polymorphic.chat.infrastructure.websocket.dto.WebSocketResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final WebSocketRequest BAD_JSON =
            new WebSocketRequest("bad_json", null, null, null);

    private final ChatRoomService roomService;
    private final AiChatService chatService;
    private final WebSocketJsonConverter jsonConverter;

    @Override
    public @NonNull Mono<Void> handle(@NonNull WebSocketSession session) {
        return authenticatedUserUuid(session)
                .flatMap(userUuid -> handleAuthenticatedSession(session, userUuid));
    }

    private Mono<UUID> authenticatedUserUuid(WebSocketSession session) {
        return session.getHandshakeInfo().getPrincipal()
                .switchIfEmpty(Mono.error(new IllegalStateException("Unauthenticated websocket")))
                .cast(Authentication.class)
                .map(Authentication::getName)
                .map(UUID::fromString);
    }

    private Mono<Void> handleAuthenticatedSession(WebSocketSession session, UUID userUuid) {
        var room = roomService.createRoom(userUuid, session.getId());

        Mono<WebSocketMessage> firstMessage = Mono.fromSupplier(() ->
                text(session, WebSocketResponse.roomCreated(room.id(), room.userUuid().toString()))
        );

        Flux<WebSocketMessage> outbound = outboundMessages(session, room);

        return session.send(Flux.concat(firstMessage, outbound))
                .doFinally(sig -> roomService.closeRoom(room.id()));
    }

    private Flux<WebSocketMessage> outboundMessages(WebSocketSession session, ChatRoomService.Room room) {
        return incomingRequests(session)
                .concatMap(req -> routeRequest(session, room, req));
    }

    private Flux<WebSocketRequest> incomingRequests(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::decodeOrBadJson);
    }

    private WebSocketRequest decodeOrBadJson(String text) {
        try {
            return jsonConverter.decodeSync(text, WebSocketRequest.class);
        } catch (Exception ignored) {
            return BAD_JSON;
        }
    }

    private Flux<WebSocketMessage> routeRequest(WebSocketSession session, ChatRoomService.Room room, WebSocketRequest req) {
        if (!"user_message".equals(req.type())) return Flux.empty();

        if (req.aiType() == null) {
            return Flux.just(text(session,
                    WebSocketResponse.error("missing_ai_type", "aiType is required")
            ));
        }

        var aiReq = AiChatRequest.userRequest(req.text());
        return callAiAndMapToWs(session, room, req, aiReq);
    }

    private @NonNull Flux<WebSocketMessage> callAiAndMapToWs(
            WebSocketSession session,
            ChatRoomService.Room room,
            WebSocketRequest req,
            AiChatRequest aiReq
    ) {
        return chatService.chatStream(req.aiType(), aiReq)
                .map(delta -> text(session, WebSocketResponse.assistantDelta(room.id(), delta)))
                .concatWithValues(text(session, WebSocketResponse.assistantDone(room.id())))
                .onErrorResume(ex -> Flux.just(
                        text(session, WebSocketResponse.error("ai_error", ex.getMessage()))
                ));
    }

    private WebSocketMessage text(WebSocketSession session, WebSocketResponse response) {
        return session.textMessage(jsonConverter.encodeSync(response));
    }
}