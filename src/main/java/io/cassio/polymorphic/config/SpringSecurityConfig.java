package io.cassio.polymorphic.config;

import io.cassio.polymorphic.auth.application.port.TokenVerifier;
import io.cassio.polymorphic.auth.infrastructure.security.JwtCookieWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SpringSecurityConfig {

    @Bean
    SecurityWebFilterChain security(ServerHttpSecurity http, TokenVerifier tokenVerifier) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/csrf").permitAll()
                        .pathMatchers("/api/user/register").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(new JwtCookieWebFilter(tokenVerifier), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
