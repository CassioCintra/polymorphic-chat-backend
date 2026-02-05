package io.cassio.polymorphic.user.infrastructure.persistence.repository;

import io.cassio.polymorphic.user.application.port.UserRepository;
import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.domain.model.Username;
import io.cassio.polymorphic.user.infrastructure.persistence.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserMongoRepository mongoRepository;

    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        return mongoRepository.existsByEmail(email.value());
    }

    @Override
    public Mono<Boolean> existsByUsername(Username username) {
        return mongoRepository.existsByUsername(username.value());
    }

    @Override
    public Mono<User> save(User user) {
        return mongoRepository.save(UserEntity.toEntity(user))
                .map(UserEntity::toDomain);
    }

    @Override
    public Mono<User> findByEmail(Email email) {
        return mongoRepository.findByEmail(email.value())
                .map(UserEntity::toDomain);
    }

    @Override
    public Mono<User> findByUsername(Username username) {
        return mongoRepository.findByUsername(username.value())
                .map(UserEntity::toDomain);
    }

}
