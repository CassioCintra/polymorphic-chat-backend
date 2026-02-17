package io.cassio.polymorphic.chat.infrastructure.websocket.dto;

import io.cassio.polymorphic.chat.domain.AiType;

public record WebSocketRequest(String type, AiType aiType, String roomId, String text) {

    public static WebSocketRequest userMessage(AiType aiType, String roomId, String text) {
        return new WebSocketRequest("user_message", aiType, roomId, text);
    }
}
