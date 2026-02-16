package io.cassio.polymorphic.auth.application.port;

import reactor.core.publisher.Mono;

public interface UserAuthQuery {

    Mono<UserAuthView> findByLogin(String login);

    record UserAuthView(
            String uuid,
            String username,
            String email,
            String hashedPassword
    ){}

}
