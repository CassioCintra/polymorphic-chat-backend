package io.cassio.polymorphic.user.infrastructure.bcrypt;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BCryptPasswordHashServiceIntegrationTest {

    @Test
    void bcrypt_shouldHashAndMatchCorrectly() {
        var encoder = new BCryptPasswordEncoder();
        var service = new BCryptPasswordHashService(encoder);

        var raw = "my-secret-password";

        var hashed = service.hash(raw);

        assertNotNull(hashed);
        assertNotEquals(raw, hashed);

        assertTrue(service.matches(raw, hashed));
        assertFalse(service.matches("wrong-password", hashed));
    }

    @Test
    void bcrypt_shouldGenerateDifferentHashesForSamePassword_butBothShouldMatch() {
        var encoder = new BCryptPasswordEncoder();
        var service = new BCryptPasswordHashService(encoder);

        var raw = "same-password";

        var hash1 = service.hash(raw);
        var hash2 = service.hash(raw);

        assertNotEquals(hash1, hash2);
        assertTrue(service.matches(raw, hash1));
        assertTrue(service.matches(raw, hash2));
    }
}
