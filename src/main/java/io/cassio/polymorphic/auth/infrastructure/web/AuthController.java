package io.cassio.polymorphic.auth.infrastructure.web;

import io.cassio.polymorphic.auth.application.port.CookieWriter;
import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.application.usecase.token.RefreshTokenService;
import io.cassio.polymorphic.auth.application.usecase.user.LoginService;
import io.cassio.polymorphic.auth.application.usecase.user.LogoutService;
import io.cassio.polymorphic.auth.infrastructure.web.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Fallback;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService login;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logout;
    private final CookieWriter cookies;
    private final TokenVerifier tokenVerifier;

    @PostMapping("/login")
    public Mono<Void> login(@RequestBody Mono<LoginRequest> body, ServerWebExchange exchange) {
        return body
                .flatMap(request -> login.execute(request.toDomain()))
                .doOnNext(tokens -> {
                    cookies.writeAccess(exchange, tokens.access());
                    cookies.writeRefresh(exchange, tokens.refresh());
                })
                .then();
    }

    @PostMapping("/refresh")
    public Mono<Void> refresh(ServerWebExchange exchange) {
        var refreshCookie = exchange.getRequest().getCookies().getFirst("refresh_token");
        if (refreshCookie == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return refreshTokenService.execute(refreshCookie.getValue())
                .doOnNext(tokens -> {
                    cookies.writeAccess(exchange, tokens.access());
                    cookies.writeRefresh(exchange, tokens.refresh());
                })
                .then();
    }

    @PostMapping("/logout")
    public Mono<Void> logout(ServerWebExchange exchange) {
        var refreshCookie = exchange.getRequest().getCookies().getFirst("refresh_token");
        cookies.clear(exchange);

        if (refreshCookie == null) return exchange.getResponse().setComplete();

        return Mono.fromSupplier(() -> tokenVerifier.verify(refreshCookie.getValue()))
                .flatMap(v -> logout.device(v.jti()))
                .then(exchange.getResponse().setComplete());
    }

    @PostMapping("/logout-all")
    public Mono<Void> logoutAll(ServerWebExchange exchange) {
        var refreshCookie = exchange.getRequest().getCookies().getFirst("refresh_token");
        cookies.clear(exchange);

        if (refreshCookie == null) return exchange.getResponse().setComplete();

        return Mono.fromSupplier(() -> tokenVerifier.verify(refreshCookie.getValue()))
                .flatMap(verified -> logout.all(verified.uuid()))
                .then(exchange.getResponse().setComplete());
    }
}
