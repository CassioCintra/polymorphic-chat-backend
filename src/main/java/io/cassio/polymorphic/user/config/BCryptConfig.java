package io.cassio.polymorphic.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties(prefix = "security.password")
public class BCryptConfig {

    @Value("${security.bcrypt.strength}")
    private int strength;

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(strength);
    }
}

