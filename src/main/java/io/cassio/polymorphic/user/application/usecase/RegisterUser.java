package io.cassio.polymorphic.user.application.usecase;

import io.cassio.polymorphic.user.application.port.PasswordHashService;
import io.cassio.polymorphic.user.domain.exception.EmailAlreadyInUseException;
import io.cassio.polymorphic.user.domain.exception.UsernameAlreadyInUseException;
import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.RegisterUserCommand;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.application.port.UserRepository;
import io.cassio.polymorphic.user.domain.model.Username;
import io.cassio.polymorphic.user.infrastructure.web.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RegisterUser {

    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public Mono<User> execute(RegisterUserCommand command) {
        return userRepository.existsByEmail(new Email(command.email()))
                .flatMap(exists -> exists
                        ? Mono.error(EmailAlreadyInUseException.of(command.email()))
                        : userRepository.existsByUsername(new Username(command.username()))
                )
                .flatMap(exists -> (boolean) exists
                        ? Mono.error(UsernameAlreadyInUseException.of(command.username()))
                        : Mono.just(command.password())
                )
                .map(passwordHashService::hash)
                .flatMap(hashedPassword -> userRepository.save(
                        User.createWithHashedPassword(
                                command.username(),
                                command.email(),
                                hashedPassword)
                ));
    }
}

