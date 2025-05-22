package com.github.phantomtrupe.weatherservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${openweathermap.geocoding-url}")
    private String geocodingBaseUrl;

    @Value("${openweathermap.onecall-url}")
    private String onecallBaseUrl;

    @Value("${openweathermap.api-key}")
    private String apiKey;

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://user-service")
                .filter(logRequest())
                .build();
    }

    @Bean
    public WebClient geoWebClient() {
        return WebClient.builder()
                .baseUrl(geocodingBaseUrl)
                .filter(logRequest())
                .build();
    }

    @Bean
    public WebClient onecallWebClient() {
        return WebClient.builder()
                .baseUrl(onecallBaseUrl)
                .filter(logRequest())
                .build();
    }

    @Bean
    public WebClient alertServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://alert-service")
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}
