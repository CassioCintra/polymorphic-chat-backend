package io.cassio.chat;

import org.springframework.boot.SpringApplication;

public class TestPolymorphicChatApplication {

	public static void main(String[] args) {
		SpringApplication.from(PolymorphicChatApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
