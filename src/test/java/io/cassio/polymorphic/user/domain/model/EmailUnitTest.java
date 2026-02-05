package io.cassio.polymorphic.user.domain.model;

import static org.junit.jupiter.api.Assertions.*;
import io.cassio.polymorphic.user.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

class EmailUnitTest {

    @Test
    void shouldThrow_whenNull() {
        assertThrows(InvalidEmailException.class, () -> new Email(null));
    }

    @Test
    void shouldThrow_whenBlankAfterTrim() {
        assertThrows(InvalidEmailException.class, () -> new Email("   "));
    }

    @Test
    void shouldThrow_whenInvalidFormat() {
        assertThrows(InvalidEmailException.class, () -> new Email("invalid-email"));
        assertThrows(InvalidEmailException.class, () -> new Email("a@b"));
        assertThrows(InvalidEmailException.class, () -> new Email("a.b.com"));
    }

    @Test
    void shouldNormalize_trimAndLowercase() {
        var email = new Email("  TeSt@Example.COM  ");
        assertEquals("test@example.com", email.value());
    }

    @Test
    void shouldAccept_validEmail() {
        var email = new Email("john.doe@example.com");
        assertEquals("john.doe@example.com", email.value());
    }
}