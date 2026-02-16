package io.cassio.polymorphic.auth.infrastructure.web;

import io.cassio.polymorphic.auth.application.port.CookieWriter;
import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.application.usecase.token.RefreshTokenService;
import io.cassio.polymorphic.auth.application.usecase.user.LoginService;
import io.cassio.polymorphic.auth.application.usecase.user.LogoutService;
import io.cassio.polymorphic.auth.domain.model.LoginResult;
import io.cassio.polymorphic.auth.infrastructure.web.dto.LoginRequest;
import io.cassio.polymorphic.config.SpringSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@Import(value = { SpringSecurityConfig.class })
@WebFluxTest(controllers = AuthController.class)
class AuthControllerTest {

    @MockitoBean LoginService login;
    @MockitoBean RefreshTokenService refreshTokenService;
    @MockitoBean LogoutService logout;
    @MockitoBean CookieWriter cookies;
    @MockitoBean TokenVerifier tokenVerifier;

    @Autowired WebTestClient client;

    @Test
    void login_shouldWriteCookies_andReturn200() {
        var loginResult = new LoginResult("acc-1", 1000L, "ref-1", 1000L);
        var request = new LoginRequest("user@mail.com", "pass");

        when(login.execute(any())).thenReturn(Mono.just(loginResult));

        client.mutateWith(csrf())
                .post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(cookies, times(1)).writeAccess(any(ServerWebExchange.class), eq("acc-1"));
        verify(cookies, times(1)).writeRefresh(any(ServerWebExchange.class), eq("ref-1"));
        verify(login, times(1)).execute(any());
        verifyNoMoreInteractions(refreshTokenService, logout, tokenVerifier);
    }

    @Test
    void refresh_shouldReturn401_whenRefreshCookieMissing() {
        client.mutateWith(csrf())
                .post().uri("/api/auth/refresh")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().isEmpty();

        verifyNoInteractions(refreshTokenService);
        verify(cookies, never()).clear(any());
        verify(cookies, never()).writeAccess(any(), anyString());
        verify(cookies, never()).writeRefresh(any(), anyString());
    }

    @Test
    void refresh_shouldCallService_writeCookies_andReturn200_whenCookiePresent() {
        var tokens = new RefreshTokenService.Result("acc-2", "ref-2");
        when(refreshTokenService.execute("refresh-value")).thenReturn(Mono.just(tokens));

        client.mutateWith(csrf())
                .post().uri("/api/auth/refresh")
                .cookie("refresh_token", "refresh-value")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(refreshTokenService).execute("refresh-value");
        verify(cookies).writeAccess(any(ServerWebExchange.class), eq("acc-2"));
        verify(cookies).writeRefresh(any(ServerWebExchange.class), eq("ref-2"));
    }

    @Test
    void logout_shouldAlwaysClearCookies_andReturn200_whenCookieMissing() {
        client.mutateWith(csrf())
                .post().uri("/api/auth/logout")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(cookies).clear(any(ServerWebExchange.class));
        verifyNoInteractions(tokenVerifier);
        verifyNoInteractions(logout);
    }

    @Test
    void logout_shouldVerifyToken_callDevice_andComplete() {
        var verified = mock(TokenVerifier.TokenVerified.class);
        when(verified.jti()).thenReturn("jti-1");
        when(tokenVerifier.verify("refresh-x")).thenReturn(verified);
        when(logout.device("jti-1")).thenReturn(Mono.empty());

        client.mutateWith(csrf())
                .post().uri("/api/auth/logout")
                .cookie("refresh_token", "refresh-x")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(cookies).clear(any(ServerWebExchange.class));
        verify(tokenVerifier).verify("refresh-x");
        verify(logout).device("jti-1");
    }

    @Test
    void logoutAll_shouldAlwaysClearCookies_andReturn200_whenCookieMissing() {
        client.mutateWith(csrf())
                .post().uri("/api/auth/logout-all")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(cookies).clear(any(ServerWebExchange.class));
        verifyNoInteractions(tokenVerifier);
        verifyNoInteractions(logout);
    }

    @Test
    void logoutAll_shouldVerifyToken_callAll_andComplete() {
        var verified = mock(TokenVerifier.TokenVerified.class);
        when(verified.uuid()).thenReturn("user-777");
        when(tokenVerifier.verify("refresh-y")).thenReturn(verified);
        when(logout.all("user-777")).thenReturn(Mono.empty());

        client.mutateWith(csrf())
                .post().uri("/api/auth/logout-all")
                .cookie("refresh_token", "refresh-y")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        verify(cookies).clear(any(ServerWebExchange.class));
        verify(tokenVerifier).verify("refresh-y");
        verify(logout).all("user-777");
    }
}
