package io.cassio.polymorphic.user.infrastructure.bcrypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordHashServiceUnitTest {

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private BCryptPasswordHashService service;

    private final String rawPassword = "plain-password";
    private final String hashedPassword = "$2a$10$fakeHash";

    @Test
    void hash_shouldDelegateToEncoderEncode() {
        when(encoder.encode(rawPassword)).thenReturn(hashedPassword);

        var result = service.hash(rawPassword);

        assertEquals(hashedPassword, result);
        verify(encoder, times(1)).encode(rawPassword);
        verifyNoMoreInteractions(encoder);
    }

    @Test
    void matches_shouldDelegateToEncoderMatches() {
        when(encoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        var result = service.matches(rawPassword, hashedPassword);

        assertTrue(result);
        verify(encoder, times(1)).matches(rawPassword, hashedPassword);
        verifyNoMoreInteractions(encoder);
    }
}
