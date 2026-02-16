package io.cassio.polymorphic.auth.infrastructure.web.dto;

import io.cassio.polymorphic.auth.domain.model.LoginCommand;

public record LoginRequest(
        String login,
        String password
) {
    public LoginCommand toDomain() {
        return new LoginCommand(
                this.login,
                this.password
        );
    }
}
