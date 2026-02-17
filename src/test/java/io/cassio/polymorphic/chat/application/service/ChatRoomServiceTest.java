package io.cassio.polymorphic.chat.application.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomServiceTest {

    @Test
    void createRoom_shouldCreateRoomWithExpectedFields_andStoreIt() throws Exception {
        ChatRoomService service = new ChatRoomService();

        UUID userUuid = UUID.randomUUID();
        String sessionId = "session-123";

        Instant before = Instant.now();
        ChatRoomService.Room room = service.createRoom(userUuid, sessionId);
        Instant after = Instant.now();

        assertThat(room).isNotNull();
        assertThat(room.id()).isNotBlank();
        assertThat(room.userUuid()).isEqualTo(userUuid);
        assertThat(room.sessionId()).isEqualTo(sessionId);

        assertThat(room.createdAt())
                .isNotNull()
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);

        Map<String, ChatRoomService.Room> rooms = getRoomsMap(service);
        assertThat(rooms).containsKey(room.id());
        assertThat(rooms.get(room.id())).isSameAs(room);
    }

    @Test
    void closeRoom_shouldRemoveExistingRoom() throws Exception {
        ChatRoomService service = new ChatRoomService();

        UUID userUuid = UUID.randomUUID();
        ChatRoomService.Room room = service.createRoom(userUuid, "session-abc");

        Map<String, ChatRoomService.Room> rooms = getRoomsMap(service);
        assertThat(rooms).containsKey(room.id());

        service.closeRoom(room.id());

        assertThat(rooms).doesNotContainKey(room.id());
    }

    @Test
    void closeRoom_shouldBeNoOpWhenRoomDoesNotExist() throws Exception {
        ChatRoomService service = new ChatRoomService();

        Map<String, ChatRoomService.Room> rooms = getRoomsMap(service);
        int sizeBefore = rooms.size();

        service.closeRoom("non-existent-room-id");

        assertThat(rooms).hasSize(sizeBefore);
    }

    @Test
    void createRoom_shouldCreateUniqueIdsForMultipleRooms() throws Exception {
        ChatRoomService service = new ChatRoomService();

        ChatRoomService.Room r1 = service.createRoom(UUID.randomUUID(), "s1");
        ChatRoomService.Room r2 = service.createRoom(UUID.randomUUID(), "s2");

        assertThat(r1.id()).isNotEqualTo(r2.id());

        Map<String, ChatRoomService.Room> rooms = getRoomsMap(service);
        assertThat(rooms).containsKeys(r1.id(), r2.id());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ChatRoomService.Room> getRoomsMap(ChatRoomService service) throws Exception {
        Field field = ChatRoomService.class.getDeclaredField("rooms");
        field.setAccessible(true);
        return (Map<String, ChatRoomService.Room>) field.get(service);
    }
}