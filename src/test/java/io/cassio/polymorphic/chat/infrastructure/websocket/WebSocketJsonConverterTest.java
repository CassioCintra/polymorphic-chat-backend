package io.cassio.polymorphic.chat.infrastructure.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

class WebSocketJsonConverterTest {

    private final WebSocketJsonConverter converter = new WebSocketJsonConverter();

    record Sample(String name, int age) {}

    @Test
    void encode_shouldSerializeObject() {
        StepVerifier.create(converter.encode(new Sample("cassio", 25)))
                .assertNext(json -> {
                    assertThat(json).contains("\"name\":\"cassio\"");
                    assertThat(json).contains("\"age\":25");
                })
                .verifyComplete();
    }

    @Test
    void encodeSync_shouldSerializeObject() {
        String json = converter.encodeSync(new Sample("cassio", 25));

        assertThat(json).contains("\"name\":\"cassio\"");
        assertThat(json).contains("\"age\":25");
    }

    @Test
    void decode_shouldDeserializeJson() {
        String json = "{\"name\":\"cassio\",\"age\":25}";

        StepVerifier.create(converter.decode(json, Sample.class))
                .assertNext(obj -> assertThat(obj).isEqualTo(new Sample("cassio", 25)))
                .verifyComplete();
    }

    @Test
    void decodeSync_shouldDeserializeJson() {
        String json = "{\"name\":\"cassio\",\"age\":25}";

        Sample obj = converter.decodeSync(json, Sample.class);

        assertThat(obj).isEqualTo(new Sample("cassio", 25));
    }

    @Test
    void decode_shouldErrorOnInvalidJson() {
        String invalid = "{not-json}";

        StepVerifier.create(converter.decode(invalid, Sample.class))
                .expectError()
                .verify();
    }

    @Test
    void decodeSync_shouldThrowIllegalArgumentExceptionOnInvalidJson() {
        String invalid = "{not-json}";

        assertThatThrownBy(() -> converter.decodeSync(invalid, Sample.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JSON payload");
    }

    @Test
    void encode_shouldErrorOnNonSerializableValue() {
        Object nonSerializable = new Object() {
            public String getBoom() { throw new RuntimeException("boom"); }
        };

        StepVerifier.create(converter.encode(nonSerializable))
                .expectError()
                .verify();
    }

    @Test
    void encodeSync_shouldThrowIllegalStateExceptionOnNonSerializableValue() {
        Object nonSerializable = new Object() {
            public String getBoom() { throw new RuntimeException("boom"); }
        };

        assertThatThrownBy(() -> converter.encodeSync(nonSerializable))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JSON serialization failed");
    }
}