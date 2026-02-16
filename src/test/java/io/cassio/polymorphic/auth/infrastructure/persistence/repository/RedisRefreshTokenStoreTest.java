package io.cassio.polymorphic.auth.infrastructure.persistence.repository;

import io.cassio.polymorphic.auth.application.port.RefreshTokenStore;
import io.cassio.polymorphic.auth.domain.exception.TokenException;
import io.cassio.polymorphic.containers.TestContainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@Import({RedisRefreshTokenStore.class})
class RedisRefreshTokenStoreTest {

    @Autowired
    RefreshTokenStore refreshTokenStore;

    @Container
    static final GenericContainer<?> redis = TestContainers.redis;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    ReactiveStringRedisTemplate redisTemplate;

    @BeforeEach
    void flush() {
        StepVerifier.create(redisTemplate.execute(conn -> conn.serverCommands().flushDb()).then())
                .verifyComplete();
    }

    @Test
    void storeThenExistsShouldReturnTrue() {
        String uuid = "user-1";
        String jti = "jti-1";

        StepVerifier.create(
                        refreshTokenStore.store(jti, uuid, 60L)
                                .then(refreshTokenStore.exists(jti))
                )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void revokeShouldRemoveJtiKey() {
        String uuid = "user-2";
        String jti = "jti-2";

        StepVerifier.create(
                        refreshTokenStore.store(jti, uuid, 60L)
                                .then(refreshTokenStore.exists(jti))
                                .flatMap(exists -> {
                                    assertThat(exists).isTrue();
                                    return refreshTokenStore.revoke(jti);
                                })
                                .then(refreshTokenStore.exists(jti))
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void revokeAllShouldRemoveAllTokensForUser() {
        String uuid = "user-3";
        List<String> jtiList = List.of("jti-a", "jti-b", "jti-c");

        StepVerifier.create(
                    refreshTokenStore.store(jtiList.get(0), uuid, 60L)
                            .then(refreshTokenStore.store(jtiList.get(1), uuid, 60L))
                            .then(refreshTokenStore.store(jtiList.get(2), uuid, 60L))
                            .then(Mono.zip(
                                    refreshTokenStore.exists(jtiList.get(0)),
                                    refreshTokenStore.exists(jtiList.get(1)),
                                    refreshTokenStore.exists(jtiList.get(2))
                            ))
                )
                .assertNext(t -> {
                    assertThat(t.getT1()).isTrue();
                    assertThat(t.getT2()).isTrue();
                    assertThat(t.getT3()).isTrue();
                })
                .verifyComplete();


        StepVerifier.create(
                    refreshTokenStore.revokeAll(uuid)
                            .then(Mono.zip(
                                    refreshTokenStore.exists(jtiList.get(0)),
                                    refreshTokenStore.exists(jtiList.get(1)),
                                    refreshTokenStore.exists(jtiList.get(2))
                            ))
                )
                .assertNext(t -> {
                    assertThat(t.getT1()).isFalse();
                    assertThat(t.getT2()).isFalse();
                    assertThat(t.getT3()).isFalse();
                })
                .verifyComplete();

    }

    @Test
    void storeShouldFailWhenTtlIsZeroOrNegative() {
        StepVerifier.create(refreshTokenStore.store("jti-x", "user-x", 0L))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(TokenException.class);
                    assertThat(err.getMessage()).contains("refreshTTL must be > 0");
                })
                .verify();

        StepVerifier.create(refreshTokenStore.store("jti-y", "user-y", -1L))
                .expectError(TokenException.class)
                .verify();
    }

    @Test
    void revokeShouldBeIdempotentWhenJtiDoesNotExist() {
        StepVerifier.create(refreshTokenStore.revoke("missing-jti"))
                .verifyComplete();
    }

    @Test
    void revokeAllShouldBeIdempotentWhenUserHasNoSet() {
        StepVerifier.create(refreshTokenStore.revokeAll("missing-user"))
                .verifyComplete();
    }
}
