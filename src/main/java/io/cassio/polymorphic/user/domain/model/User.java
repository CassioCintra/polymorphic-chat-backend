package io.cassio.polymorphic.user.domain.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record User(
        UUID uuid,
        Username username,
        Email email,
        HashedPassword password
) {
    public static User createWithHashedPassword(String username, String email, String hashedPassword) {
        return User.builder()
                .uuid(UUID.randomUUID())
                .username(new Username(username))
                .email(new Email(email))
                .password(new HashedPassword(hashedPassword))
                .build();
    }

}
