package io.cassio.polymorphic.auth.infrastructure.security;

import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtCookieWebFilterTest {

    TokenVerifier verifier = mock(TokenVerifier.class);

    JwtCookieWebFilter filter = new JwtCookieWebFilter(verifier);

    private static ServerWebExchange exchangeWithAccessCookie(String tokenValue) {
        var request = MockServerHttpRequest.get("/")
                .cookie(new HttpCookie("access_token", tokenValue))
                .build();
        return MockServerWebExchange.from(request);
    }

    private static ServerWebExchange exchangeWithoutCookie() {
        var request = MockServerHttpRequest.get("/").build();
        return MockServerWebExchange.from(request);
    }

    @Test
    void shouldPassThroughWhenCookieMissing() {
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchangeWithoutCookie(), chain))
                .verifyComplete();

        verify(chain).filter(any());
        verifyNoInteractions(verifier);
    }

    @Test
    void shouldSetAuthenticationWhenAccessTokenIsValidAndTypeAccess() {
        String jwt = "valid.jwt";
        String uuid = "user-123";

        var verified = mock(TokenVerifier.TokenVerified.class);
        when(verified.type()).thenReturn(TokenVerifier.TokenType.ACCESS);
        when(verified.uuid()).thenReturn(uuid);

        when(verifier.verify(jwt)).thenReturn(verified);

        WebFilterChain chain = ex ->
                ReactiveSecurityContextHolder.getContext()
                        .mapNotNull(SecurityContext::getAuthentication)
                        .hasElement()
                        .doOnNext(hasAuth -> { assertThat(hasAuth).isTrue(); })
                        .then();

        StepVerifier.create(filter.filter(exchangeWithAccessCookie(jwt), chain))
                .verifyComplete();

        verify(verifier).verify(jwt);
    }

    @Test
    void shouldNotSetAuthenticationWhenTokenTypeIsNotAccess() {
        String jwt = "refresh.jwt";

        var verified = mock(TokenVerifier.TokenVerified.class);
        when(verified.type()).thenReturn(TokenVerifier.TokenType.REFRESH);
        when(verified.uuid()).thenReturn("user-x");

        when(verifier.verify(jwt)).thenReturn(verified);

        WebFilterChain chain = ex ->
                ReactiveSecurityContextHolder.getContext()
                        .mapNotNull(SecurityContext::getAuthentication)
                        .hasElement()
                        .doOnNext(hasAuth -> assertThat(hasAuth).isFalse())
                        .then();

        StepVerifier.create(filter.filter(exchangeWithAccessCookie(jwt), chain))
                .verifyComplete();

        verify(verifier).verify(jwt);
    }

    @Test
    void shouldPassThroughWhenVerifierThrows() {
        String jwt = "invalid.jwt";

        when(verifier.verify(jwt)).thenThrow(new RuntimeException("bad token"));

        WebFilterChain chain = ex ->
                ReactiveSecurityContextHolder.getContext()
                        .mapNotNull(SecurityContext::getAuthentication)
                        .hasElement()
                        .doOnNext(hasAuth -> assertThat(hasAuth).isFalse())
                        .then();

        StepVerifier.create(filter.filter(exchangeWithAccessCookie(jwt), chain))
                .verifyComplete();

        verify(verifier).verify(jwt);
    }

    @Test
    void shouldNotBreakChainEvenWhenVerifierThrows() {
        String jwt = "invalid.jwt";
        when(verifier.verify(jwt)).thenThrow(new IllegalArgumentException("bad"));

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchangeWithAccessCookie(jwt), chain))
                .verifyComplete();

        verify(chain).filter(any());
        verify(verifier).verify(jwt);
    }
}
