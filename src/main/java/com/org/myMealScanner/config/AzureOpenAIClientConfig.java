package com.org.myMealScanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class AzureOpenAIClientConfig {

    @Value("${azure.oai.endpoint}")
    private String endpoint;

    HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)              // 연결 시도 5초
            .responseTimeout(Duration.ofSeconds(30))                         // 응답 전체 30초
            .doOnConnected(conn -> conn
                    .addHandlerLast(new ReadTimeoutHandler(30)));
    @Bean
    public WebClient azureOpenAiWebClient() {
        return WebClient.builder()
                .baseUrl(endpoint) // https://solution-ai.openai.azure.com
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
