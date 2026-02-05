package io.cassio.polymorphic.user.application.usecase;

import io.cassio.polymorphic.user.application.port.PasswordHashService;
import io.cassio.polymorphic.user.application.port.UserRepository;
import io.cassio.polymorphic.user.domain.exception.EmailAlreadyInUseException;
import io.cassio.polymorphic.user.domain.exception.UsernameAlreadyInUseException;
import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.RegisterUserCommand;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.domain.model.Username;
import io.cassio.polymorphic.user.infrastructure.web.dto.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUnitTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordHashService passwordHashService;

    @InjectMocks
    RegisterUser registerUser;

    @Captor
    ArgumentCaptor<User> userCaptor;

    private final RegisterUserCommand command = new RegisterUserCommand("Test Username", "test@email.com", "123456");;

    @Test
    void execute_shouldError_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(any(Email.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(registerUser.execute(command))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(EmailAlreadyInUseException.class);
                    assertThat(err.getMessage()).contains(command.email());
                })
                .verify();

        verify(userRepository).existsByEmail(any(Email.class));
        verify(userRepository, never()).existsByUsername(any(Username.class));
        verify(passwordHashService, never()).hash(anyString());
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository, passwordHashService);
    }

    @Test
    void execute_shouldError_whenUsernameAlreadyExists() {
        when(userRepository.existsByEmail(any(Email.class)))
                .thenReturn(Mono.just(false));
        when(userRepository.existsByUsername(any(Username.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(registerUser.execute(command))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(UsernameAlreadyInUseException.class);
                    assertThat(err.getMessage()).contains(command.username());
                })
                .verify();

        verify(userRepository).existsByEmail(any(Email.class));
        verify(userRepository).existsByUsername(any(Username.class));
        verify(passwordHashService, never()).hash(anyString());
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository, passwordHashService);
    }

    @Test
    void execute_shouldHashPassword_andSaveUser_whenValid() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(Mono.just(false));
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(Mono.just(false));
        when(passwordHashService.hash("123456")).thenReturn("$2b$hashed");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(registerUser.execute(command))
                .assertNext(saved -> {
                    assertThat(saved).isNotNull();
                    assertThat(saved.uuid()).isNotNull();
                    assertThat(saved.username().value()).isEqualTo(command.username());
                    assertThat(saved.email().value()).isEqualTo(command.email());
                    assertThat(saved.password().value()).isEqualTo("$2b$hashed");
                })
                .verifyComplete();

        verify(userRepository).existsByEmail(any(Email.class));
        verify(userRepository).existsByUsername(any(Username.class));
        verify(passwordHashService).hash("123456");

        verify(userRepository).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertThat(userToSave.username().value()).isEqualTo(command.username());
        assertThat(userToSave.email().value()).isEqualTo(command.email());
        assertThat(userToSave.password().value()).isEqualTo("$2b$hashed");

        verifyNoMoreInteractions(userRepository, passwordHashService);
    }
}
