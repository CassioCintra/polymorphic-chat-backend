package io.cassio.polymorphic.auth.application.port;

import reactor.core.publisher.Mono;

public interface RefreshTokenStore {

    Mono<Void> store(String jti, String uuid, Long refreshTTL);
    Mono<Boolean> exists(String jti);
    Mono<Void> revoke(String jti);
    Mono<Void> revokeAll(String uuid);

}
