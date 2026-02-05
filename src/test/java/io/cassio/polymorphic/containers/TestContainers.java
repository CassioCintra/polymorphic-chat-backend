package io.cassio.polymorphic.containers;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers
public class TestContainers {

    @Container
    public static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:8.2.4").withReuse(true);

    static {
        mongoContainer.start();
    }

    private TestContainers() {}
}
