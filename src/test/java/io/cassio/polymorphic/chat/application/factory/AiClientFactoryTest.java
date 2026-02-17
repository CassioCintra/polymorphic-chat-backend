package io.cassio.polymorphic.chat.application.factory;

import io.cassio.polymorphic.chat.application.port.AiClient;
import io.cassio.polymorphic.chat.domain.AiType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Import(AiClientFactory.class)
@ComponentScan(basePackages = "io.cassio.polymorphic.chat.infrastructure.ai")
public class AiClientFactoryTest {

    @Autowired private AiClientFactory factory;
    @Autowired private List<AiClient> clients;
    @Autowired private ApplicationContext context;

    @EnumSource(AiType.class)
    @ParameterizedTest(name = "Should return the correct client from spring context for {0}")
    void shouldReturnCorrectClientFromSpringContext(AiType type) {
        AiClient expectedClient = resolveSingleClientByType(type);

        assertThat(context.getBeansOfType(AiClient.class).values()).contains(expectedClient);
        assertThat(factory.get(type)).isSameAs(expectedClient);
        assertThat(factory.get(type).getType()).isEqualTo(type);
    }

    @Test
    void shouldNotHaveDuplicateClientsForSameType() {
        assertThat(clients)
                .extracting(AiClient::getType)
                .doesNotHaveDuplicates();
    }

    private AiClient resolveSingleClientByType(AiType type) {
        List<AiClient> clientMatches = clients.stream()
                .filter(c -> c.getType() == type)
                .toList();

        assertThat(clientMatches)
                .as("Should exist exactly 1 AiClient bean for type %s", type)
                .hasSize(1);

        return clientMatches.getFirst();
    }

}