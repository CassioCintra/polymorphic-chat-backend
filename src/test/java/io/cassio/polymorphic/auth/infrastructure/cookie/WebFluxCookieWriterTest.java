package io.cassio.polymorphic.auth.infrastructure.cookie;

import io.cassio.polymorphic.auth.infrastructure.config.CookieProperties;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class WebFluxCookieWriterTest {

    private WebFluxCookieWriter cookieWriter;

    @BeforeEach
    void setup() {
        TokenProperties tokenProperties = new TokenProperties(
                new TokenProperties.Access(3600L),
                new TokenProperties.Refresh(7200L)
        );
        CookieProperties cookieProperties = new CookieProperties(
                true,
                "Lax"
        );

        cookieWriter = new WebFluxCookieWriter(cookieProperties, tokenProperties);
    }

    @Test
    void writeAccessShouldWriteAccessCookie() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );
        cookieWriter.writeAccess(exchange, "access.jwt");

        MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();

        ResponseCookie cookie = response.getCookies().getFirst("access_token");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("access.jwt");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    void writeRefreshShouldWriteRefreshCookie() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );
        cookieWriter.writeRefresh(exchange, "refresh.jwt");

        MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();

        ResponseCookie cookie = response.getCookies().getFirst("refresh_token");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo("refresh.jwt");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(7200);
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    void clearShouldRemoveBothCookies() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );
        cookieWriter.clear(exchange);

        MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();

        ResponseCookie access = response.getCookies().getFirst("access_token");
        ResponseCookie refresh = response.getCookies().getFirst("refresh_token");
        assertThat(access).isNotNull();
        assertThat(refresh).isNotNull();
        assertThat(access.getValue()).isEmpty();
        assertThat(refresh.getValue()).isEmpty();
        assertThat(access.getMaxAge().getSeconds()).isZero();
        assertThat(refresh.getMaxAge().getSeconds()).isZero();
        assertThat(access.isHttpOnly()).isTrue();
        assertThat(refresh.isHttpOnly()).isTrue();
    }

}