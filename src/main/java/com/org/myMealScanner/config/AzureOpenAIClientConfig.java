package com.org.myMealScanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AzureOpenAIClientConfig {

    @Value("${azure.oai.endpoint}")
    private String endpoint;

    @Bean
    public WebClient azureOpenAiWebClient() {
        return WebClient.builder()
                .baseUrl(endpoint)
                .build();
    }
}
