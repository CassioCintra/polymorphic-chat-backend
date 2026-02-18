package io.cassio.polymorphic.chat.infrastructure.ai.cohere;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiMessage;
import io.cassio.polymorphic.chat.infrastructure.ai.cohere.config.CohereProperties;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.jupiter.api.*;

class CohereAiClientTest {

    private final WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    private CohereProperties cohereProperties;

    @BeforeEach
    void setup() {
        wireMock.start();
        cohereProperties = new CohereProperties(
                "http://localhost:" + wireMock.port(),
                "test-api-key",
                "command-r"
        );
        configureFor("localhost", wireMock.port());
    }

    @AfterEach
    void teardown() {
        if (wireMock.isRunning()) {
            wireMock.stop();
        }
    }

    @Test
    void chatStream_shouldSendCorrectRequest_andEmitOnlyContentDeltaTexts() {
        var client = new CohereAiClient(cohereProperties);
        var sse =
            "data: {\"type\":\"content-delta\",\"delta\":{\"message\":{\"content\":{\"text\":\"Olá\"}}}}\n\n" +
            "data: {\"type\":\"message-end\"}\n\n" +
            "data: {\"type\":\"content-delta\",\"delta\":{\"message\":{\"content\":{\"text\":\" mundo\"}}}}\n\n";
        mockClientServer(sse);

        var request = new AiChatRequest(List.of(new AiMessage("user", "oi")));
        Flux<String> flux = client.chatStream(request);

        StepVerifier.create(flux)
                .expectNext("Olá")
                .expectNext(" mundo")
                .verifyComplete();

        wireMock.verify(postRequestedFor(urlEqualTo("/v2/chat"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer test-api-key"))
                .withHeader(HttpHeaders.ACCEPT, containing("text/event-stream"))
                .withHeader(HttpHeaders.CONTENT_TYPE, containing("application/json"))
                .withRequestBody(matchingJsonPath("$.stream", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.model", equalTo("command-r")))
                .withRequestBody(matchingJsonPath("$.messages[0].role", equalTo("user")))
                .withRequestBody(matchingJsonPath("$.messages[0].content", equalTo("oi")))
        );
    }

    @Test
    void chatStream_shouldIgnoreEventsWithoutData_andNonContentDeltaTypes() {
        var client = new CohereAiClient(cohereProperties);

        String sse =
            ": keep-alive\n\n" +
            "data: {\"type\":\"message-end\"}\n\n" +
            "data: {\"type\":\"content-delta\",\"delta\":{\"message\":{\"content\":{\"text\":\"ok\"}}}}\n\n";
        mockClientServer(sse);

        var request = new AiChatRequest(List.of(new AiMessage("user", "oi")));
        StepVerifier.create(client.chatStream(request))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    void chatStream_shouldErrorWhenEventDataIsNotJson() {
        var client = new CohereAiClient(cohereProperties);
        String sse =
            "data: {not-json}\n\n" +
            "data: {\"type\":\"content-delta\",\"delta\":{\"message\":{\"content\":{\"text\":\"ok\"}}}}\n\n";
        mockClientServer(sse);

        var request = new AiChatRequest(List.of(new AiMessage("user", "oi")));

        StepVerifier.create(client.chatStream(request))
                .expectError()
                .verify();
    }

    private void mockClientServer(String sse) {
        wireMock.stubFor(post(urlEqualTo("/v2/chat"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/event-stream;charset=UTF-8")
                        .withBody(sse)));
    }
}