package com.github.phantomtrupe.alertservice.controller;

import com.github.phantomtrupe.alertservice.model.AlertDto;
import com.github.phantomtrupe.alertservice.model.AlertRecord;
import com.github.phantomtrupe.alertservice.service.AlertProcessingService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/alerts")
public class AlertController {
    private final AlertProcessingService service;

    public AlertController(AlertProcessingService service) {
        this.service = service;
    }

    @PostMapping("/weather")
    public Mono<Void> receiveWeatherAlert(@RequestBody AlertDto dto) {
        return service.processWeatherAlert(dto);
    }

    @GetMapping("/history")
    public Flux<AlertRecord> getHistory() {
        return service.getAlertHistory();
    }
}
