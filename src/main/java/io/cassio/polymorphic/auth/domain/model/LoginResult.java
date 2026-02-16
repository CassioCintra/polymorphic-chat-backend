package io.cassio.polymorphic.auth.domain.model;

public record LoginResult(
        String access,
        Long accessTTL,
        String refresh,
        Long refreshTTL
) {}
