package io.cassio.polymorphic.user.application.port;

import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.domain.model.Username;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<Boolean> existsByEmail(Email email);

    Mono<Boolean> existsByUsername(Username username);

    Mono<User> save(User user);

    Mono<User> findByEmail(Email email);

    Mono<User> findByUsername(Username username);
}
