package io.cassio.polymorphic.auth.infrastructure.security;

import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import java.util.List;

public class JwtCookieWebFilter implements WebFilter {

    private final TokenVerifier verifier;

    public JwtCookieWebFilter(TokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("access_token");
        if (cookie == null) return chain.filter(exchange);

        try {
            var token = verifier.verify(cookie.getValue());
            if (token.type() != TokenVerifier.TokenType.ACCESS) return chain.filter(exchange);

            var auth = new UsernamePasswordAuthenticationToken(token.uuid(), null, List.of());

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        } catch (Exception ignored) {
            return chain.filter(exchange);
        }
    }
}
