package io.cassio.polymorphic.auth.application.usecase.user;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import io.cassio.polymorphic.auth.application.port.TokenIssuer;
import io.cassio.polymorphic.auth.application.port.UserAuthQuery;
import io.cassio.polymorphic.auth.domain.model.LoginCommand;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import io.cassio.polymorphic.user.application.port.PasswordHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private UserAuthQuery userAuthQuery;
    @Mock private PasswordHashService passwordHashService;
    @Mock private TokenIssuer tokenIssuer;
    @Mock private RefreshTokenStore refreshTokenStore;

    private LoginService service;

    @BeforeEach
    void setup() {
        TokenProperties tokenProperties = new TokenProperties(
                new TokenProperties.Access(3600L),
                new TokenProperties.Refresh(7200L)
        );

        service = new LoginService(
                userAuthQuery,
                passwordHashService,
                tokenIssuer,
                refreshTokenStore,
                tokenProperties
        );
    }

    @Test
    void shouldFailWhenUserNotFound() {
        var command = new LoginCommand("login", "pass");
        when(userAuthQuery.findByLogin("login")).thenReturn(Mono.empty());

        StepVerifier.create(service.execute(command))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                    assert "Invalid credentials".equals(err.getMessage());
                })
                .verify();

        verify(userAuthQuery).findByLogin("login");
        verifyNoInteractions(passwordHashService, tokenIssuer, refreshTokenStore);
    }

    @Test
    void shouldFailWhenPasswordDoesNotMatch() {
        var command = new LoginCommand("login", "wrong-pass");
        var user = new UserAuthQuery.UserAuthView("uuid-1", "user", "email@x.com", "hashed");

        when(userAuthQuery.findByLogin("login")).thenReturn(Mono.just(user));
        when(passwordHashService.matches("wrong-pass", "hashed")).thenReturn(false);

        StepVerifier.create(service.execute(command))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                    assert "Invalid credentials".equals(err.getMessage());
                })
                .verify();

        verify(userAuthQuery).findByLogin("login");
        verify(passwordHashService).matches("wrong-pass", "hashed");
        verifyNoInteractions(tokenIssuer, refreshTokenStore);
    }

    @Test
    void shouldReturnLoginResultAndStoreRefreshTokenOnSuccess() {
        var cmd = new LoginCommand("login", "pass");
        var user = new UserAuthQuery.UserAuthView("uuid-1", "user", "email@x.com", "hashed");

        when(userAuthQuery.findByLogin("login")).thenReturn(Mono.just(user));
        when(passwordHashService.matches("pass", "hashed")).thenReturn(true);
        when(tokenIssuer.issueAccess(eq("uuid-1"), anyString())).thenReturn("access.jwt");
        when(tokenIssuer.issueRefresh(eq("uuid-1"), anyString())).thenReturn("refresh.jwt");
        when(refreshTokenStore.store(anyString(), eq("uuid-1"), eq(7200L))).thenReturn(Mono.empty());

        StepVerifier.create(service.execute(cmd))
                .assertNext(result -> {
                    assert "access.jwt".equals(result.access());
                    assert 3600L == result.accessTTL();
                    assert "refresh.jwt".equals(result.refresh());
                    assert 7200L == result.refreshTTL();
                })
                .verifyComplete();

        verify(userAuthQuery).findByLogin("login");
        verify(passwordHashService).matches("pass", "hashed");
        verify(tokenIssuer).issueAccess(eq("uuid-1"), anyString());
        verify(tokenIssuer).issueRefresh(eq("uuid-1"), anyString());

        ArgumentCaptor<String> jtiCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenStore).store(jtiCaptor.capture(), eq("uuid-1"), eq(7200L));
        assert jtiCaptor.getValue() != null && !jtiCaptor.getValue().isBlank();
    }

    @Test
    void shouldPropagateErrorWhenStoreFails() {
        var cmd = new LoginCommand("login", "pass");
        var user = new UserAuthQuery.UserAuthView("uuid-1", "user", "email@x.com", "hashed");

        when(userAuthQuery.findByLogin("login")).thenReturn(Mono.just(user));
        when(passwordHashService.matches("pass", "hashed")).thenReturn(true);
        when(tokenIssuer.issueAccess(eq("uuid-1"), anyString())).thenReturn("access.jwt");
        when(tokenIssuer.issueRefresh(eq("uuid-1"), anyString())).thenReturn("refresh.jwt");
        when(refreshTokenStore.store(anyString(), eq("uuid-1"), eq(7200L)))
                .thenReturn(Mono.error(new RuntimeException("Redis down")));

        StepVerifier.create(service.execute(cmd))
                .expectErrorSatisfies(err -> {
                    assert err instanceof RuntimeException;
                    assert err.getMessage().contains("Redis down");
                })
                .verify();

        verify(refreshTokenStore).store(anyString(), eq("uuid-1"), eq(7200L));
    }
}
