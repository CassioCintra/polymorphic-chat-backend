package io.cassio.polymorphic.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CookieProperties.class})
public class CookiePropertiesConfig {
}
