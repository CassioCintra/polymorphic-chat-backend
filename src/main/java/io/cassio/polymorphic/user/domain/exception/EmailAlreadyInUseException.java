package io.cassio.polymorphic.user.domain.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    private EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }

    public static EmailAlreadyInUseException of(String email) {
        return new EmailAlreadyInUseException(email);
    }
}
