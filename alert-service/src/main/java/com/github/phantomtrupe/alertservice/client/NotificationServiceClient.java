package com.github.phantomtrupe.alertservice.client;

import com.github.phantomtrupe.alertservice.model.NotificationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationServiceClient {
    private final WebClient webClient;

    public NotificationServiceClient(WebClient notificationServiceWebClient) {
        this.webClient = notificationServiceWebClient;
    }

    public Mono<Void> sendNotification(NotificationRequest request) {
        return webClient.post()
                .uri("/notifications")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.empty()); // swallow errors for individual calls
    }
}
