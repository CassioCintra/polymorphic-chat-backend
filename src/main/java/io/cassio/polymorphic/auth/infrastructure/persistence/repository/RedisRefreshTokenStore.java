package io.cassio.polymorphic.auth.infrastructure.persistence.repository;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;

import io.cassio.polymorphic.auth.domain.exception.TokenException;
import lombok.RequiredArgsConstructor;
import org.apache.el.parser.TokenMgrError;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final ReactiveStringRedisTemplate redis;

    private String jtiKey(String jti) { return "auth:refresh:jti:" + jti; }
    private String userKey(String uuid) { return "auth:refresh:user:" + uuid; }

    @Override
    public Mono<Void> store(String jti, String uuid, Long refreshTTL) {
        if (refreshTTL <= 0) {
            return Mono.error(TokenException.invalidRefreshTtl());
        }

        var jKey = jtiKey(jti);
        var uKey = userKey(uuid);

        return redis.opsForValue()
                .set(jKey, uuid, Duration.ofSeconds(refreshTTL))
                .then(redis.opsForSet().add(uKey, jti).then())
                .then();
    }

    @Override
    public Mono<Boolean> exists(String jti) {
        return redis.hasKey(jtiKey(jti));
    }

    @Override
    public Mono<Void> revoke(String jti) {
        var jKey = jtiKey(jti);

        return redis.opsForValue().get(jKey)
                .flatMap(uuid ->
                        redis.opsForSet()
                                .remove(userKey(uuid), jti)
                                .then(redis.delete(jKey))
                                .then()
                )
                .then();
    }

    @Override
    public Mono<Void> revokeAll(String uuid) {
        var uKey = userKey(uuid);

        return redis.opsForSet()
                .members(uKey)
                .map(this::jtiKey)
                .as(redis::delete)
                .then(redis.delete(uKey))
                .then();
    }

}
