package io.cassio.polymorphic.user.domain.model;

import io.cassio.polymorphic.user.domain.exception.InvalidUsernameException;

public record Username(String value) {

    private static final Integer MINIMUM_USERNAME_SIZE = 6;

    public Username{
        if (value == null || value.isBlank()) {
            throw InvalidUsernameException.nullOrEmpty();
        }

        if (value.length() < MINIMUM_USERNAME_SIZE) {
            throw InvalidUsernameException.tooShort(MINIMUM_USERNAME_SIZE);
        }
    }
}
