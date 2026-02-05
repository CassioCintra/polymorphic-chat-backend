package io.cassio.polymorphic.user.domain.model;

import io.cassio.polymorphic.user.domain.exception.InvalidUsernameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsernameUnitTest {

    @Test
    void shouldThrow_whenNull() {
        assertThrows(InvalidUsernameException.class, () -> new Username(null));
    }

    @Test
    void shouldThrow_whenBlank() {
        assertThrows(InvalidUsernameException.class, () -> new Username("   "));
        assertThrows(InvalidUsernameException.class, () -> new Username(""));
    }

    @Test
    void shouldThrow_whenTooShort() {
        assertThrows(InvalidUsernameException.class, () -> new Username("abcde"));
    }

    @Test
    void shouldAccept_whenMinSizeOrMore() {
        var username = new Username("abcdef");
        assertEquals("abcdef", username.value());

        var username2 = new Username("abcdefgh");
        assertEquals("abcdefgh", username2.value());
    }
}
