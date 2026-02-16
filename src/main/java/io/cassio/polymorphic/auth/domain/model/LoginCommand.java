package io.cassio.polymorphic.auth.domain.model;

public record LoginCommand(
        String login,
        String password
) {}
