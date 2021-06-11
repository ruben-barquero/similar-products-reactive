package com.rbb.similarproductsreactive.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient(@Value("${existingApis.server.url}") final String serverUrl) {
		return WebClient.create(serverUrl);
	}
	
}
