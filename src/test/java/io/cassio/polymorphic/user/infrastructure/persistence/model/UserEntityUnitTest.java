package io.cassio.polymorphic.user.infrastructure.persistence.model;

import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.HashedPassword;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.domain.model.Username;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityUnitTest {

    private final UUID uuid = UUID.randomUUID();
    private final String username = "TestUsername";
    private final String email = "test@example.com";
    private final String hashedPassword = "$2b$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm";
    
    @Test
    void toEntity_shouldMapDomainToEntityCorrectly() {
        var user = User.builder()
                .uuid(uuid)
                .username(new Username(username))
                .email(new Email(email))
                .password(new HashedPassword(hashedPassword))
                .build();

        var entity = UserEntity.toEntity(user);

        assertEquals(uuid.toString(), entity.getUuid());
        assertEquals(username, entity.getUsername());
        assertEquals(email, entity.getEmail());
        assertEquals(
                hashedPassword,
                entity.getHashedPassword()
        );
    }

    @Test
    void toDomain_shouldMapEntityToDomainCorrectly() {
        var entity = new UserEntity(
                null,
                uuid.toString(),
                username,
                hashedPassword,
                email
        );

        var user = entity.toDomain();

        assertEquals(uuid, user.uuid());
        assertEquals(username, user.username().value());
        assertEquals(email, user.email().value());
        assertTrue(user.password().value().startsWith("$2b$"));
    }

    @Test
    void roundTrip_shouldPreserveData() {
        var original = User.createWithHashedPassword(
                username,
                email,
                hashedPassword
        );

        var entity = UserEntity.toEntity(original);
        var restored = entity.toDomain();

        assertEquals(original.uuid(), restored.uuid());
        assertEquals(original.username().value(), restored.username().value());
        assertEquals(original.email().value(), restored.email().value());
        assertEquals(original.password().value(), restored.password().value());
    }
}
