package io.cassio.polymorphic.chat.application.config;

import io.cassio.polymorphic.chat.infrastructure.websocket.ChatWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WebSocketConfig.class)
class WebSocketConfigTest {

    @MockitoBean
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private SimpleUrlHandlerMapping mapping;

    @Autowired
    private WebSocketHandlerAdapter adapter;

    @Test
    void shouldExposeWebSocketHandlerAdapterBean() {
        assertThat(adapter).isNotNull();
    }

    @Test
    void shouldMapChatWebSocketEndpointToHandler() {
        var mapped = mapping.getUrlMap().get("/api/ws/chat");

        assertThat(mapped)
                .isNotNull()
                .isSameAs(chatWebSocketHandler);

        assertThat(mapping.getOrder()).isEqualTo(-1);
    }
}