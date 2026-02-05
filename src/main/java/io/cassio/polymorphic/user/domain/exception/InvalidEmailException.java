package io.cassio.polymorphic.user.domain.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }

    public static InvalidEmailException emptyEmail(){
        return new InvalidEmailException("Email cannot be empty");
    }

    public static InvalidEmailException nullEmail(){
        return new InvalidEmailException("Email cannot be null");
    }

    public static InvalidEmailException invalidEmail(){
        return new InvalidEmailException("Invalid email format");
    }
}
