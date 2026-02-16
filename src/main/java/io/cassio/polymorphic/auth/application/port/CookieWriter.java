package io.cassio.polymorphic.auth.application.port;

import org.springframework.web.server.ServerWebExchange;

public interface CookieWriter {

    void writeAccess(ServerWebExchange exchange, String jwt);
    void writeRefresh(ServerWebExchange exchange, String jwt);
    void clear(ServerWebExchange exchange);

}
