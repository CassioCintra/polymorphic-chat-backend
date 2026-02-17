package io.cassio.polymorphic.chat.application.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatRoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(UUID userUuid, String sessionId) {
        String roomId = UUID.randomUUID().toString();
        Room room = new Room(roomId, userUuid, sessionId, Instant.now());
        rooms.put(roomId, room);
        return room;
    }

    public void closeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public record Room(String id, UUID userUuid, String sessionId, Instant createdAt) {}
}
