package io.cassio.polymorphic.user.domain.model;

import io.cassio.polymorphic.user.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashedPasswordUnitTest {

    @Test
    void shouldThrow_whenNullOrBlank() {
        assertThrows(InvalidPasswordException.class, () -> new HashedPassword(null));
        assertThrows(InvalidPasswordException.class, () -> new HashedPassword(""));
        assertThrows(InvalidPasswordException.class, () -> new HashedPassword("   "));
    }

    @Test
    void shouldThrow_whenPrefixIsInvalid() {
        assertThrows(InvalidPasswordException.class, () -> new HashedPassword("not-a-bcrypt-hash"));
        assertThrows(InvalidPasswordException.class, () -> new HashedPassword("$2x$10$whatever"));
    }

    @Test
    void shouldAccept_whenPrefixIsValid_2a() {
        var hp = new HashedPassword("$2a$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm");
        assertTrue(hp.value().startsWith("$2a$"));
    }

    @Test
    void shouldAccept_whenPrefixIsValid_2b() {
        var hp = new HashedPassword("$2b$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm");
        assertTrue(hp.value().startsWith("$2b$"));
    }

    @Test
    void shouldAccept_whenPrefixIsValid_2y() {
        var hp = new HashedPassword("$2y$10$abcdefghijklmnopqrstuv0123456789ABCDEFGHijklm");
        assertTrue(hp.value().startsWith("$2y$"));
    }
}
