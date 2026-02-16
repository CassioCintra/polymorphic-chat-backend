package io.cassio.polymorphic.auth.infrastructure.web.exception;

import io.cassio.polymorphic.auth.domain.exception.CredentialException;
import io.cassio.polymorphic.auth.domain.exception.TokenException;
import io.cassio.polymorphic.common.web.exception.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(0)
@RestControllerAdvice(basePackages = "io.cassio.polymorphic.auth")
public class AuthExceptionHandler {

    @ExceptionHandler(CredentialException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCredentials(
            CredentialException ex,
            ServerWebExchange exchange
    ) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.of(
                        "AUTH_INVALID_CREDENTIALS",
                        401,
                        "Unauthorized",
                        ex.getMessage(),
                        exchange.getRequest().getPath().value()
                )
        ));
    }

    @ExceptionHandler(TokenException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleToken(
            TokenException ex,
            ServerWebExchange exchange
    ) {
        var path = exchange.getRequest().getPath().value();
        if ("refreshTTL must be > 0".equals(ex.getMessage())) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ErrorResponse.of(
                            "AUTH_REFRESH_TTL_INVALID",
                            500,
                            "Internal Server Error",
                            "Internal authentication error.",
                            path
                    )
            ));
        }
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.of(
                        tokenCodeFromMessage(ex.getMessage()),
                        401,
                        "Unauthorized",
                        ex.getMessage(),
                        path
                )
        ));
    }

    private static String tokenCodeFromMessage(String msg) {
        if (msg == null) return "AUTH_TOKEN_ERROR";

        return switch (msg) {
            case "Invalid token type" -> "AUTH_INVALID_TOKEN_TYPE";
            case "Refresh token revoked" -> "AUTH_REFRESH_REVOKED";
            case "Missing type claim" -> "AUTH_MISSING_CLAIM";
            default -> {
                if (msg.startsWith("Invalid type claim:")) yield "AUTH_INVALID_CLAIM";
                yield "AUTH_TOKEN_ERROR";
            }
        };
    }
}
