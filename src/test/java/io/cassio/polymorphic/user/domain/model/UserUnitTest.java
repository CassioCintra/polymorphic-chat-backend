package io.cassio.polymorphic.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserUnitTest {

    @Test
    void createWithHashedPassword_shouldCreateUserWithUuidAndValueObjects() {
        var user = User.createWithHashedPassword(
                "testUsername",
                "  TEST@EXAMPLE.COM ",
                "$2b$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm"
        );

        assertNotNull(user.uuid());
        assertNotNull(user.username());
        assertNotNull(user.email());
        assertNotNull(user.password());

        assertEquals("testUsername", user.username().value());
        assertEquals("test@example.com", user.email().value());
        assertTrue(user.password().value().startsWith("$2b$"));
    }

    @Test
    void createWithHashedPassword_shouldThrow_whenInvalidUsername() {
        assertThrows(RuntimeException.class, () ->
                User.createWithHashedPassword(
                        "abc",
                        "user@example.com",
                        "$2b$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm"
                )
        );
    }

    @Test
    void createWithHashedPassword_shouldThrow_whenInvalidEmail() {
        assertThrows(RuntimeException.class, () ->
                User.createWithHashedPassword(
                        "valid1",
                        "invalid-email",
                        "$2b$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm"
                )
        );
    }

    @Test
    void createWithHashedPassword_shouldThrow_whenInvalidHashedPassword() {
        assertThrows(RuntimeException.class, () ->
                User.createWithHashedPassword(
                        "valid1",
                        "user@example.com",
                        "plain-password"
                )
        );
    }
}
