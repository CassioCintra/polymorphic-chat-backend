package io.cassio.polymorphic.user.infrastructure.web;

import io.cassio.polymorphic.factory.UserTestFactory;
import io.cassio.polymorphic.user.application.usecase.RegisterUser;
import io.cassio.polymorphic.user.domain.model.RegisterUserCommand;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.infrastructure.web.dto.RegisterUserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RegisterUser registerUser;

    private final User domainUser = UserTestFactory.generateUser();
    private final RegisterUserCommand request = new RegisterUserCommand(
            domainUser.username().value(),
            domainUser.email().value(),
            "RawPassword"
    );

    @Test
    void registerUser_shouldReturn201AndMappedResponse_whenSuccess() {
        when(registerUser.execute(any(RegisterUserCommand.class))).thenReturn(Mono.just(domainUser));

        var expected = RegisterUserResponse.from(domainUser);

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/user/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RegisterUserResponse.class)
                .isEqualTo(expected);

        var captor = ArgumentCaptor.forClass(RegisterUserCommand.class);
        verify(registerUser, times(1)).execute(captor.capture());

        assertEquals(request.username(), captor.getValue().username());
        assertEquals(request.email(), captor.getValue().email());
        assertEquals(request.password(), captor.getValue().password());

        verifyNoMoreInteractions(registerUser);
    }

    @Test
    void registerUser_shouldReturn5xx_whenUseCaseErrors() {
        when(registerUser.execute(any(RegisterUserCommand.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/api/user/register")
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(registerUser, times(1)).execute(any(RegisterUserCommand.class));
    }
}
