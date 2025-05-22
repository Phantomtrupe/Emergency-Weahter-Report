package com.github.phantomtrupe.weatherservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient userServiceWebClient) {
        this.webClient = userServiceWebClient;
    }

    public Flux<String> getDistinctCities() {
        return webClient.get()
                .uri("/users/cities")
                .retrieve()
                .bodyToFlux(String.class);
    }
}
