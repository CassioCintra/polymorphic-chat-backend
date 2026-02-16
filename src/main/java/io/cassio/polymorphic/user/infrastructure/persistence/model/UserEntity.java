package io.cassio.polymorphic.user.infrastructure.persistence.model;

import io.cassio.polymorphic.auth.application.port.UserAuthQuery;
import io.cassio.polymorphic.user.domain.model.Email;
import io.cassio.polymorphic.user.domain.model.HashedPassword;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.domain.model.Username;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("users")
@Getter @Setter
@AllArgsConstructor
public class UserEntity {

    @Id
    private String id;
    @Indexed(unique = true)
    private String  uuid;
    @Indexed(unique = true)
    private String username;
    private String hashedPassword;
    private String email;


    public static UserEntity toEntity(User user) {
        return new UserEntity(
                null,
                user.uuid().toString(),
                user.username().value(),
                user.password().value(),
                user.email().value()
        );
    }

    public User toDomain() {
        return User.builder()
                .uuid(UUID.fromString(this.getUuid()))
                .username(new Username(this.getUsername()))
                .email(new Email(this.getEmail()))
                .password(new HashedPassword(this.getHashedPassword()))
                .build();
    }

    public UserAuthQuery.UserAuthView toAuthView(){
        return new UserAuthQuery.UserAuthView(
                this.getUuid(),
                this.getUsername(),
                this.getEmail(),
                this.getHashedPassword()
        );
    }
}
