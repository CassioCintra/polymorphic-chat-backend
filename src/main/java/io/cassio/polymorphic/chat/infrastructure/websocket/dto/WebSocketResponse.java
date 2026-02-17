package io.cassio.polymorphic.chat.infrastructure.websocket.dto;

public record WebSocketResponse<T>(String type, T data) {

    public static WebSocketResponse<RoomCreated> roomCreated(String roomId, String userUuid) {
        return new WebSocketResponse<>("room_created", new RoomCreated(roomId, userUuid));
    }

    public static WebSocketResponse<AssistantDelta> assistantDelta(String roomId, String delta) {
        return new WebSocketResponse<>("assistant_delta", new AssistantDelta(roomId, delta));
    }

    public static WebSocketResponse<AssistantDone> assistantDone(String roomId) {
        return new WebSocketResponse<>("assistant_done", new AssistantDone(roomId));
    }

    public static WebSocketResponse<Error> error(String code, String message) {
        return new WebSocketResponse<>("error", new Error(code, message));
    }

    public record RoomCreated(String roomId, String userUuid) {}
    public record AssistantDelta(String roomId, String delta) {}
    public record AssistantDone(String roomId) {}
    public record Error(String code, String message) {}
}
