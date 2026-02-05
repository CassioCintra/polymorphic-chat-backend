package io.cassio.polymorphic.user.domain.model;

import io.cassio.polymorphic.user.domain.exception.InvalidEmailException;

public record Email(String value) {

    private static final String REGEX = ".+@.+\\..+";

    public Email {
        if (value == null) {
            throw InvalidEmailException.nullEmail();
        }

        value = value.trim().toLowerCase();

        if (value.isBlank()) {
            throw InvalidEmailException.emptyEmail();
        }

        if (!value.matches(REGEX)) {
            throw InvalidEmailException.invalidEmail();
        }
    }
}

