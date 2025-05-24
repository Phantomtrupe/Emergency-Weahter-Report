package com.github.phantomtrupe.notifierservice.controller;

import com.github.phantomtrupe.notifierservice.model.NotificationRequest;
import com.github.phantomtrupe.notifierservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@Validated
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    public Mono<ResponseEntity<Void>> sendNotification(@Valid @RequestBody NotificationRequest request) {
        return notificationService.send(request)
                .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("OK"));
    }
}
