package io.cassio.polymorphic.common.web.middleware;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingWebFilter implements WebFilter {

    public static final String REQ_ID_HEADER = "X-Request-Id";
    public static final String REQ_ID_ATTR = "reqId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant start = Instant.now();

        String reqId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(REQ_ID_HEADER))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        exchange.getAttributes().put(REQ_ID_ATTR, reqId);

        exchange.getResponse().getHeaders().set(REQ_ID_HEADER, reqId);

        ServerHttpRequest req = exchange.getRequest();
        String method = req.getMethod().name();
        String path = req.getURI().getRawPath();
        String query = req.getURI().getRawQuery();
        String fullPath = (query == null || query.isBlank()) ? path : path + "?" + query;

        String ip = Optional.ofNullable(req.getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("-");

        String ua = Optional.ofNullable(req.getHeaders().getFirst("User-Agent")).orElse("-");

        log.info("[{}] --> {} {} ip={} ua=\"{}\"", reqId, method, fullPath, ip, ua);

        return chain.filter(exchange)
                .doOnError(err -> {
                    long ms = Duration.between(start, Instant.now()).toMillis();
                    log.warn("[{}] <-- {} {} ERROR after {}ms: {}",
                            reqId, method, fullPath, ms, err.toString());
                })
                .doFinally(signal -> {
                    long ms = Duration.between(start, Instant.now()).toMillis();
                    Integer status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : null;
                    String statusStr = status != null ? status.toString() : "-";
                    log.info("[{}] <-- {} {} {} in {}ms",
                            reqId, method, fullPath, statusStr, ms);
                });
    }
}
