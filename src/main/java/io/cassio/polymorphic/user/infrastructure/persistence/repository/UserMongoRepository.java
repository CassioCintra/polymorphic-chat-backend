package io.cassio.polymorphic.user.infrastructure.persistence.repository;

import io.cassio.polymorphic.user.infrastructure.persistence.model.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserMongoRepository extends ReactiveMongoRepository<UserEntity, UUID> {
        Mono<Boolean> existsByEmail(String email);
        Mono<UserEntity> findByEmail(String email);

        Mono<Boolean> existsByUsername(String username);
        Mono<UserEntity> findByUsername(String username);
}
