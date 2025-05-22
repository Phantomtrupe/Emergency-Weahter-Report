package com.github.phantomtrupe.weatherservice.scheduler;

import com.github.phantomtrupe.weatherservice.service.WeatherAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Component
public class WeatherAlertScheduler {
    private static final Logger log = LoggerFactory.getLogger(WeatherAlertScheduler.class);
    private final WeatherAlertService weatherAlertService;

    public WeatherAlertScheduler(WeatherAlertService weatherAlertService) {
        this.weatherAlertService = weatherAlertService;
    }

    @PostConstruct
    public void schedule() {
        Flux.interval(Duration.ofHours(1))
            .doOnNext(tick -> log.info("Triggering weather alert workflow"))
            .flatMap(tick -> weatherAlertService.processAlerts())
            .subscribe(
                null,
                error -> log.error("Error in weather alert scheduler", error)
            );
    }
}
