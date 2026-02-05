package io.cassio.polymorphic.user.infrastructure.persistence.repository;

import io.cassio.polymorphic.containers.TestContainers;
import io.cassio.polymorphic.factory.UserTestFactory;
import io.cassio.polymorphic.user.domain.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.test.StepVerifier;

@DataMongoTest
@Import({ UserRepositoryAdapter.class })
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter adapter;

    @Autowired
    private UserMongoRepository mongoRepository;

    private final User user = UserTestFactory.generateUser();

    private static final MongoDBContainer mongo = TestContainers.mongoContainer;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        StepVerifier.create(mongoRepository.deleteAll()).verifyComplete();

        StepVerifier.create(adapter.save(this.user))
                .expectNextCount(1)
                .verifyComplete();
    }

    @AfterEach
    void tearDown() {
        StepVerifier.create(mongoRepository.deleteAll()).verifyComplete();
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenExists() {
        var persistedEmail = this.user.email();

        StepVerifier.create(adapter.existsByEmail(persistedEmail))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenExists() {
        var persistedUsername = this.user.username();

        StepVerifier.create(adapter.existsByUsername(persistedUsername))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void save_shouldPersist() {
        var anotherUser = UserTestFactory.generateAnotherUser();

        StepVerifier.create(adapter.save(anotherUser))
                .expectNextMatches(saved -> saved.uuid() != null)
                .verifyComplete();
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        var emailToFind = this.user.email();

        StepVerifier.create(adapter.findByEmail(emailToFind))
                .expectNextMatches(found -> found.uuid().equals(user.uuid()))
                .verifyComplete();
    }

    @Test
    void findByUsername_shouldReturnUser_whenExists() {
        var usernameToFind = this.user.username();

        StepVerifier.create(adapter.findByUsername(usernameToFind))
                .expectNextMatches(found -> found.uuid().equals(user.uuid()))
                .verifyComplete();
    }
}