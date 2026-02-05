package io.cassio.polymorphic.user.application.port;

public interface PasswordHashService {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}