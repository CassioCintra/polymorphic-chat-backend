package io.cassio.polymorphic.user.domain.exception;

public class UsernameAlreadyInUseException extends RuntimeException {

    private UsernameAlreadyInUseException(String email) {
        super("Username already in use: " + email);
    }

    public static UsernameAlreadyInUseException of(String email) {
        return new UsernameAlreadyInUseException(email);
    }
}
