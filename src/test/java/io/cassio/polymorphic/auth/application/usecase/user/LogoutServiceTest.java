package io.cassio.polymorphic.auth.application.usecase.user;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Test
    void deviceShouldRevokeRefreshJti() {
        var service = new LogoutService(refreshTokenStore);

        when(refreshTokenStore.revoke("jti-1")).thenReturn(Mono.empty());

        StepVerifier.create(service.device("jti-1"))
                .verifyComplete();

        verify(refreshTokenStore).revoke("jti-1");
        verifyNoMoreInteractions(refreshTokenStore);
    }

    @Test
    void allShouldRevokeAllForSubjectUuid() {
        var service = new LogoutService(refreshTokenStore);

        when(refreshTokenStore.revokeAll("uuid-1")).thenReturn(Mono.empty());

        StepVerifier.create(service.all("uuid-1"))
                .verifyComplete();

        verify(refreshTokenStore).revokeAll("uuid-1");
        verifyNoMoreInteractions(refreshTokenStore);
    }

    @Test
    void deviceShouldPropagateErrors() {
        var service = new LogoutService(refreshTokenStore);

        when(refreshTokenStore.revoke("jti-err"))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        StepVerifier.create(service.device("jti-err"))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                    assert err.getMessage().contains("redis down");
                })
                .verify();

        verify(refreshTokenStore).revoke("jti-err");
        verifyNoMoreInteractions(refreshTokenStore);
    }

    @Test
    void allShouldPropagateErrors() {
        var service = new LogoutService(refreshTokenStore);

        when(refreshTokenStore.revokeAll("uuid-err"))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        StepVerifier.create(service.all("uuid-err"))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                    assert err.getMessage().contains("redis down");
                })
                .verify();

        verify(refreshTokenStore).revokeAll("uuid-err");
        verifyNoMoreInteractions(refreshTokenStore);
    }
}
