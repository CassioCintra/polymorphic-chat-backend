package io.cassio.polymorphic.user.domain.model;

import io.cassio.polymorphic.user.domain.exception.InvalidPasswordException;

import java.util.List;

public record HashedPassword(String value) {

    private static final List<String> VALID_PREFIXES = List.of("$2a$", "$2b$", "$2y$");

    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw InvalidPasswordException.passwordNullOrEmpty();
        }

        if (VALID_PREFIXES.stream().noneMatch(value::startsWith)) {
            throw InvalidPasswordException.invalidHash();
        }
    }
}
