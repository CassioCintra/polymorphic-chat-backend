package io.cassio.polymorphic.factory;

import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.HashedPassword;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.domain.model.Username;

import java.util.UUID;

public class UserTestFactory {

    public static User generateUser(){
        return User.builder()
                .uuid(UUID.randomUUID())
                .username(new Username("Username"))
                .email(new Email("email@email.com"))
                .password(new HashedPassword("$2a$10$hashedPassword"))
                .build();
    }

    public static User generateAnotherUser(){
        return User.builder()
                .uuid(UUID.randomUUID())
                .username(new Username("Another Username"))
                .email(new Email("another@email.com"))
                .password(new HashedPassword("$2a$10$anotherHashedPassword"))
                .build();
    }


}
