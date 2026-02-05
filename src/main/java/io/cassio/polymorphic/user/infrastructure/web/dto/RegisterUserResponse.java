package io.cassio.polymorphic.user.infrastructure.web.dto;

import io.cassio.polymorphic.user.domain.model.User;

import java.util.UUID;

public record RegisterUserResponse(
        UUID uuid,
        String username,
        String email
) {
    public static RegisterUserResponse from(User user) {
        return new RegisterUserResponse(
                user.uuid(),
                user.username().value(),
                user.email().value()
        );
    }
}

