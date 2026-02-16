package io.cassio.polymorphic.auth.infrastructure.cookie;

import io.cassio.polymorphic.auth.application.port.CookieWriter;
import io.cassio.polymorphic.auth.infrastructure.config.CookieProperties;
import io.cassio.polymorphic.auth.infrastructure.config.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@RequiredArgsConstructor
public class WebFluxCookieWriter implements CookieWriter {

    private final CookieProperties cookieProperties;
    private final TokenProperties tokenProperties;

    @Override
    public void writeAccess(ServerWebExchange exchange, String jwt) {
        exchange.getResponse()
                .addCookie(ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path("/")
                .sameSite(cookieProperties.sameSite())
                .maxAge(tokenProperties.access().expiration()).build());
    }

    @Override
    public void writeRefresh(ServerWebExchange exchange, String jwt) {
        exchange.getResponse()
                .addCookie(ResponseCookie.from("refresh_token", jwt)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path("/")
                .sameSite(cookieProperties.sameSite())
                .maxAge(tokenProperties.refresh().expiration()).build());
    }

    @Override
    public void clear(ServerWebExchange exchange) {
        exchange.getResponse()
                .addCookie(ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path("/")
                .sameSite(cookieProperties.sameSite())
                .maxAge(0).build());
        exchange.getResponse()
                .addCookie(ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .path("/")
                .sameSite(cookieProperties.sameSite())
                .maxAge(0).build());
    }
}
