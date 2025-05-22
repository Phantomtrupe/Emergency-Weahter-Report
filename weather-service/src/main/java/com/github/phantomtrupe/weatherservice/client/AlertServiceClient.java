package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.AlertDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AlertServiceClient {
    private final WebClient webClient;

    public AlertServiceClient(WebClient alertServiceWebClient) {
        this.webClient = alertServiceWebClient;
    }

    public Mono<Void> sendAlert(AlertDto alert) {
        return webClient.post()
                .uri("/alerts/weather")
                .bodyValue(alert)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
