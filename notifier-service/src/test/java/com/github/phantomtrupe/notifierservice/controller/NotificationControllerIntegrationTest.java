package com.github.phantomtrupe.notifierservice.controller;

import com.github.phantomtrupe.notifierservice.model.NotificationRequest;
import com.github.phantomtrupe.notifierservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerIntegrationTest {
    private WebTestClient webTestClient;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(notificationController).build();
    }

    @Test
    void sendNotification_withValidRequest_shouldReturnOk() {
        NotificationRequest req = new NotificationRequest();
        req.setEmail("test@example.com");
        req.setPhoneNumber("+10000000000");
        req.setMessage("Alert message");

        when(notificationService.send(any(NotificationRequest.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk();

        verify(notificationService, times(1)).send(any(NotificationRequest.class));
    }

    @Test
    void sendNotification_withMissingFields_shouldReturnBadRequest() {
        NotificationRequest req = new NotificationRequest();
        req.setEmail(""); // invalid
        req.setPhoneNumber(null);
        req.setMessage("");

        webTestClient.post()
                .uri("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();

        verify(notificationService, never()).send(any(NotificationRequest.class));
    }

    @Test
    void healthEndpoint_shouldReturnOk() {
        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("OK");
    }
}
