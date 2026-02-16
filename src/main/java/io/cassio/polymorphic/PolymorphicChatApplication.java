package io.cassio.polymorphic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
public class PolymorphicChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolymorphicChatApplication.class, args);
	}

}
