package io.cassio.polymorphic.chat.infrastructure.ai.gemini;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cassio.polymorphic.chat.domain.AiChatRequest;
import io.cassio.polymorphic.chat.domain.AiMessage;
import io.cassio.polymorphic.chat.infrastructure.ai.gemini.config.GeminiProperties;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

class GeminiAiClientTest {

    private final WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());
    private GeminiProperties geminiProperties;

    @BeforeEach
    void setup() {
        wireMock.start();
        geminiProperties = new GeminiProperties(
                "http://localhost:" + wireMock.port(),
                "test-api-key",
                "gemini-2.5-flash-lite"
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
    void chatStream_shouldSendCorrectRequest_andEmitTextsFromCandidates() {
        var client = new GeminiAiClient(geminiProperties);
        String sse =
                "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Olá\"}]}}]}\n\n" +
                "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\" mundo\"}]}}]}\n\n";
        mockClientServer(sse);

        var request = new AiChatRequest(List.of(new AiMessage("user", "oi")));
        Flux<String> flux = client.chatStream(request);

        StepVerifier.create(flux)
                .expectNext("Olá")
                .expectNext(" mundo")
                .verifyComplete();

        wireMock.verify(postRequestedFor(urlPathEqualTo("/v1beta/models/gemini-2.5-flash-lite:streamGenerateContent"))
                .withQueryParam("alt", equalTo("sse"))
                .withHeader("x-goog-api-key", equalTo("test-api-key"))
                .withHeader(HttpHeaders.ACCEPT, containing("text/event-stream"))
                .withHeader(HttpHeaders.CONTENT_TYPE, containing("application/json"))
                .withRequestBody(matchingJsonPath("$.contents[0].role", equalTo("user")))
                .withRequestBody(matchingJsonPath("$.contents[0].parts[0].text", equalTo("oi")))
        );
    }

    @Test
    void chatStream_shouldIgnoreEventsWithoutData_keepAliveComments() {
        var client = new GeminiAiClient(geminiProperties);
        String sse =
                ": keep-alive\n\n" +
                "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}\n\n";

        mockClientServer(sse);

        var request = new AiChatRequest(List.of(new AiMessage("user", "oi")));

        StepVerifier.create(client.chatStream(request))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    void chatStream_shouldErrorWhenEventDataIsNotJson() {
        var client = new GeminiAiClient(geminiProperties);
        String sse =
                "data: {not-json}\n\n" +
                "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}\n\n";
        mockClientServer(sse);

        var request = new AiChatRequest(List.of(new AiMessage("user", "oi")));

        StepVerifier.create(client.chatStream(request))
                .expectError()
                .verify();
    }

    private void mockClientServer(String sse) {
        wireMock.stubFor(post(urlPathEqualTo("/v1beta/models/gemini-2.5-flash-lite:streamGenerateContent"))
                .withQueryParam("alt", equalTo("sse"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/event-stream")
                        .withBody(sse)));
    }
}