package io.cassio.polymorphic.common.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Order(1000)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        List<FieldViolation> violations = ex.getFieldErrors().stream()
                .map(err -> new FieldViolation(err.getField(), err.getDefaultMessage()))
                .toList();
        String instance = exchange.getRequest().getPath().value();
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validation(instance, violations)));
    }

    @ExceptionHandler({ServerWebInputException.class, DecodingException.class})
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(
            Exception ex,
            ServerWebExchange exchange
    ) {
        log.warn("Bad request: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        "BAD_REQUEST",
                        400,
                        "Bad Request",
                        "Request body is invalid.",
                        exchange.getRequest().getPath().value()
                )));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatus(
            ResponseStatusException ex,
            ServerWebExchange exchange
    ) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        return Mono.just(ResponseEntity
                .status(status)
                .body(ErrorResponse.of(
                        "HTTP_" + status.value(),
                        status.value(),
                        status.getReasonPhrase(),
                        (ex.getReason() != null && !ex.getReason().isBlank())
                            ? ex.getReason()
                            : status.getReasonPhrase(),
                        exchange.getRequest().getPath().value()
                )));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnexpected(
            Throwable ex,
            ServerWebExchange exchange
    ) {
        log.error("Unhandled exception", ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "INTERNAL_ERROR",
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred.",
                        exchange.getRequest().getPath().value()
                )));
    }
}
