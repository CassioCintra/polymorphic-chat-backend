package io.cassio.polymorphic.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers
public class TestContainers {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:8.2.4").withReuse(true);

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>("redis:8.4.0").withExposedPorts(6379).withReuse(true);

    static {
        mongoContainer.start();
        redis.start();
    }

    private TestContainers() {}
}
