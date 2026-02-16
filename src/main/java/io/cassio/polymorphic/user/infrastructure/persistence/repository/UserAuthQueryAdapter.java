package io.cassio.polymorphic.user.infrastructure.persistence.repository;

import io.cassio.polymorphic.auth.application.port.UserAuthQuery;
import io.cassio.polymorphic.user.infrastructure.persistence.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserAuthQueryAdapter implements UserAuthQuery {

    private final UserMongoRepository mongoRepository;

    @Override
    public Mono<UserAuthView> findByLogin(String login) {
        return mongoRepository.findByEmail(login)
                .switchIfEmpty(mongoRepository.findByUsername(login))
                .map(UserEntity::toAuthView);
    }
}
