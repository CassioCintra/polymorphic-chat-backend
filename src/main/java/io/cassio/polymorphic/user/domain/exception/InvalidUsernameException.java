package io.cassio.polymorphic.user.domain.exception;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException(String message) {
        super(message);
    }

    public static InvalidUsernameException nullOrEmpty() {
        return new InvalidUsernameException("Username cannot be null or empty");
    }

    public static InvalidUsernameException tooShort(int min) {
        return new InvalidUsernameException(
                "Username must have at least " + min + " characters"
        );
    }
}
