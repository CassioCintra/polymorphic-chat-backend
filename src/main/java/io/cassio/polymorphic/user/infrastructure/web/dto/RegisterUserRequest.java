package io.cassio.polymorphic.user.infrastructure.web.dto;

import io.cassio.polymorphic.user.domain.model.RegisterUserCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password
) {
    public RegisterUserCommand toRegisterUserCommand() {
        return RegisterUserCommand.builder()
                .username(this.username)
                .email(this.email)
                .password(this.password)
                .build();
    }
}
