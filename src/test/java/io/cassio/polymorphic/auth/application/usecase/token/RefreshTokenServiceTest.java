package io.cassio.polymorphic.auth.application.usecase.token;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import io.cassio.polymorphic.auth.application.port.TokenIssuer;
import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.domain.exception.TokenException;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private TokenVerifier tokenVerifier;
    @Mock private TokenIssuer tokenIssuer;
    @Mock private RefreshTokenStore refreshTokenStore;

    private RefreshTokenService service;

    @BeforeEach
    void setup() {
        TokenProperties tokenProperties = new TokenProperties(
                new TokenProperties.Access(3600L),
                new TokenProperties.Refresh(7200L)
        );

        service = new RefreshTokenService(tokenVerifier, tokenIssuer, refreshTokenStore, tokenProperties);
    }

    @Test
    void shouldFailWhenTokenTypeIsNotRefresh() {
        var refreshJwt = "refresh.jwt";
        var verified = new TokenVerifier.TokenVerified("user-uuid", "jti-123", TokenVerifier.TokenType.ACCESS);

        when(tokenVerifier.verify(refreshJwt)).thenReturn(verified);

        StepVerifier.create(service.execute(refreshJwt))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                })
                .verify();

        verify(tokenVerifier).verify(refreshJwt);
        verifyNoInteractions(refreshTokenStore);
        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void shouldFailWhenRefreshTokenIsRevoked() {
        var refreshJwt = "refresh.jwt";
        var verified = new TokenVerifier.TokenVerified("user-uuid", "jti-123", TokenVerifier.TokenType.REFRESH);

        when(tokenVerifier.verify(refreshJwt)).thenReturn(verified);
        when(refreshTokenStore.exists("jti-123")).thenReturn(Mono.just(false));

        StepVerifier.create(service.execute(refreshJwt))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                })
                .verify();

        verify(tokenVerifier).verify(refreshJwt);
        verify(refreshTokenStore).exists("jti-123");
        verify(refreshTokenStore, never()).revoke(anyString());
        verify(refreshTokenStore, never()).store(anyString(), anyString(), anyLong());

        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void shouldIssueNewTokensAndRotateRefreshOnSuccess() {
        var refreshJwt = "refresh.jwt";
        var verified = new TokenVerifier.TokenVerified("user-uuid", "old-jti", TokenVerifier.TokenType.REFRESH);

        when(tokenVerifier.verify(refreshJwt)).thenReturn(verified);
        when(refreshTokenStore.exists("old-jti")).thenReturn(Mono.just(true));
        when(tokenIssuer.issueAccess(eq("user-uuid"), anyString())).thenReturn("new-access");
        when(tokenIssuer.issueRefresh(eq("user-uuid"), anyString())).thenReturn("new-refresh");
        when(refreshTokenStore.revoke("old-jti")).thenReturn(Mono.empty());
        when(refreshTokenStore.store(anyString(), eq("user-uuid"), eq(7200L))).thenReturn(Mono.empty());

        StepVerifier.create(service.execute(refreshJwt))
                .assertNext(result -> {
                    // record Result(String access, String refresh)
                    assert result.access().equals("new-access");
                    assert result.refresh().equals("new-refresh");
                })
                .verifyComplete();

        verify(tokenVerifier).verify(refreshJwt);
        verify(refreshTokenStore).exists("old-jti");
        verify(tokenIssuer).issueAccess(eq("user-uuid"), anyString());
        verify(tokenIssuer).issueRefresh(eq("user-uuid"), anyString());
        verify(refreshTokenStore).revoke("old-jti");
        verify(refreshTokenStore).store(anyString(), eq("user-uuid"), eq(7200L));

        InOrder inOrder = inOrder(refreshTokenStore);
        inOrder.verify(refreshTokenStore).revoke("old-jti");
        inOrder.verify(refreshTokenStore).store(anyString(), eq("user-uuid"), eq(7200L));
    }

    @Test
    void shouldPropagateErrorIfStoreFails() {
        var refreshJwt = "refresh.jwt";
        var verified = new TokenVerifier.TokenVerified("user-uuid", "old-jti", TokenVerifier.TokenType.REFRESH);

        when(tokenVerifier.verify(refreshJwt)).thenReturn(verified);
        when(refreshTokenStore.exists("old-jti")).thenReturn(Mono.just(true));
        when(tokenIssuer.issueAccess(eq("user-uuid"), anyString())).thenReturn("new-access");
        when(tokenIssuer.issueRefresh(eq("user-uuid"), anyString())).thenReturn("new-refresh");
        when(refreshTokenStore.revoke("old-jti")).thenReturn(Mono.empty());
        when(refreshTokenStore.store(anyString(), eq("user-uuid"), eq(7200L)))
                .thenReturn(Mono.error(new RuntimeException("Redis down")));

        StepVerifier.create(service.execute(refreshJwt))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                    assert err.getMessage().contains("Redis down");
                })
                .verify();

        verify(refreshTokenStore).revoke("old-jti");
        verify(refreshTokenStore).store(anyString(), eq("user-uuid"), eq(7200L));
    }

}
