package io.cassio.polymorphic.user.infrastructure.persistence.repository;

import io.cassio.polymorphic.containers.TestContainers;
import io.cassio.polymorphic.factory.UserTestFactory;
import io.cassio.polymorphic.user.domain.model.User;
import io.cassio.polymorphic.user.infrastructure.persistence.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(UserAuthQueryAdapter.class)
class UserAuthQueryAdapterIntegrationTest {

    @Autowired UserMongoRepository mongoRepository;
    @Autowired UserAuthQueryAdapter adapter;

    private final User user = UserTestFactory.generateUser();;

    private static final MongoDBContainer mongo = TestContainers.mongoContainer;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        StepVerifier.create(mongoRepository.deleteAll()).verifyComplete();
        StepVerifier.create(mongoRepository.save(UserEntity.toEntity(user)))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByLogin_shouldReturnUserAuthView_whenFoundByEmail() {
        String login = user.email().value();

        StepVerifier.create(adapter.findByLogin(login))
                .assertNext(view -> {
                         assertThat(view.uuid()).isEqualTo(user.uuid().toString());
                         assertThat(view.hashedPassword()).isEqualTo(user.password().value());
                         assertThat(view.email()).isEqualTo(user.email().value());
                         assertThat(view.username()).isEqualTo(user.username().value());
                })
                .verifyComplete();
    }

    @Test
    void findByLogin_shouldReturnUserAuthView_whenFoundByUsername() {
        String login = user.username().value();

        StepVerifier.create(adapter.findByLogin(login))
                .assertNext(view -> {
                    assertThat(view.uuid()).isEqualTo(user.uuid().toString());
                    assertThat(view.hashedPassword()).isEqualTo(user.password().value());
                    assertThat(view.email()).isEqualTo(user.email().value());
                    assertThat(view.username()).isEqualTo(user.username().value());
                })
                .verifyComplete();
    }

    @Test
    void findByLogin_shouldReturnEmpty_whenMissing() {
        StepVerifier.create(adapter.findByLogin("missing"))
                .expectNextCount(0)
                .verifyComplete();
    }

}
