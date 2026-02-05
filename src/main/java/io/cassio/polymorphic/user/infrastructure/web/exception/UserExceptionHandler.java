package io.cassio.polymorphic.user.infrastructure.web.exception;

import io.cassio.polymorphic.common.web.exception.ErrorResponse;
import io.cassio.polymorphic.user.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(0)
@RestControllerAdvice(basePackages = "io.cassio.polymorphic.user")
public class UserExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleEmailAlreadyInUse(
            EmailAlreadyInUseException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Email already in use: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(
                        "EMAIL_ALREADY_IN_USE",
                        409,
                        "Conflict",
                        ex.getMessage(),
                        exchange.getRequest().getPath().value()
                )
        ));
    }

    @ExceptionHandler(UsernameAlreadyInUseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUsernameAlreadyInUse(
            UsernameAlreadyInUseException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Username already in use: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(
                        "USERNAME_ALREADY_IN_USE",
                        409,
                        "Conflict",
                        ex.getMessage(),
                        exchange.getRequest().getPath().value()
                )
        ));
    }

    @ExceptionHandler(InvalidEmailException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidEmail(
            InvalidEmailException ex,
            ServerWebExchange exchange
    ) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(
                        "INVALID_EMAIL",
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        exchange.getRequest().getPath().value()
                )
        ));
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidUsername(
            InvalidUsernameException ex,
            ServerWebExchange exchange
    ) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(
                        "INVALID_USERNAME",
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        exchange.getRequest().getPath().value()
                )
        ));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidPassword(
            InvalidPasswordException ex,
            ServerWebExchange exchange
    ) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(
                        "INVALID_PASSWORD",
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        exchange.getRequest().getPath().value()
                )
        ));
    }
}
