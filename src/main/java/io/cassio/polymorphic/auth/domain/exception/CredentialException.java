package io.cassio.polymorphic.auth.domain.exception;

public class CredentialException extends RuntimeException {
    public CredentialException(String message) {
        super(message);
    }

    public static CredentialException invalidCredentials(){
        return new CredentialException("Invalid credentials");
    }
}
