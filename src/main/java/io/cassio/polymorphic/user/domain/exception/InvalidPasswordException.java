package io.cassio.polymorphic.user.domain.exception;

public class InvalidPasswordException extends RuntimeException {
    private InvalidPasswordException(String message) {
        super(message);
    }

    public static InvalidPasswordException passwordNullOrEmpty(){
        return new InvalidPasswordException("Password cannot be null or empty");
    }

    public static InvalidPasswordException invalidHash(){
        return new InvalidPasswordException("Password must be a valid hash");
    }
}
