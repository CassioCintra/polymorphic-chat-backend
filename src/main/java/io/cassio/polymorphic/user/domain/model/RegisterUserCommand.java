package io.cassio.polymorphic.user.domain.model;

import lombok.Builder;

@Builder
public record RegisterUserCommand(
        String username,
        String email,
        String password
) {}
