package com.github.phantomtrupe.weatherservice.service;

import com.github.phantomtrupe.weatherservice.client.AlertServiceClient;
import com.github.phantomtrupe.weatherservice.client.GeoClient;
import com.github.phantomtrupe.weatherservice.client.OneCallClient;
import com.github.phantomtrupe.weatherservice.client.UserServiceClient;
import com.github.phantomtrupe.commons.dto.AlertDto;
import com.github.phantomtrupe.weatherservice.model.GeocodeResponse;
import com.github.phantomtrupe.weatherservice.model.OneCallAlert;
import com.github.phantomtrupe.weatherservice.model.OneCallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class WeatherAlertService {
    private static final Logger log = LoggerFactory.getLogger(WeatherAlertService.class);

    private final UserServiceClient   userClient;
    private final GeoClient           geoClient;
    private final OneCallClient       oneCallClient;
    private final AlertServiceClient  alertClient;

    public WeatherAlertService(UserServiceClient userClient,
                               GeoClient geoClient,
                               OneCallClient oneCallClient,
                               AlertServiceClient alertClient) {
        this.userClient    = userClient;
        this.geoClient     = geoClient;
        this.oneCallClient = oneCallClient;
        this.alertClient   = alertClient;
    }

    /**
     * Main entry point triggered by your scheduler.
     * 1. Fetch distinct cities from user-service.
     * 2. Geocode each city.
     * 3. Fetch One Call alerts.
     * 4. Map into your AlertDto.
     * 5. Forward to alert-service.
     */
    // Return Mono<Void> that completes when all cities are processed
    public Mono<Void> processAlerts() {
        return userClient.getDistinctCities()
                .distinct()
                .flatMap(this::handleCity, /* concurrency */ 4)
                .then();
    }

    private Flux<Void> handleCity(String city) {
        return geoClient.geocode(city)
                .next()  // take first geocode result only
                .onErrorResume(e -> Mono.empty()) // skip geocoding errors
                .flatMapMany(coords -> fetchAndForward(city, coords))
                .onErrorContinue((ex, obj) -> log.warn("Skipping city {} due to error: {}", city, ex.getMessage()));
    }

    private Flux<Void> fetchAndForward(String city, GeocodeResponse coords) {
        double lat = coords.getLat();
        double lon = coords.getLon();

        return oneCallClient.getOneCall(lat, lon)
                .onErrorResume(e -> {
                    log.warn("Skipping OneCall for {} due to error: {}", city, e.getMessage());
                    return Mono.empty();
                })
                .flatMapMany(this::extractAlerts)
                .map(alert -> toDto(city, alert))
                .flatMap(dto -> alertClient.sendAlert(dto)
                        .doOnSuccess(v -> log.info("Forwarded '{}' for {}", dto.getEvent(), city))
                        .doOnError(err -> log.error("Failed to forward {} for {}: {}", dto.getEvent(), city, err.getMessage()))
                );
    }

    private Flux<OneCallAlert> extractAlerts(OneCallResponse resp) {
        if (resp.getAlerts() == null || resp.getAlerts().isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(resp.getAlerts());
    }

    private AlertDto toDto(String city, OneCallAlert in) {
        AlertDto dto = new AlertDto();
        dto.setCity(city);
        dto.setEvent(in.getEvent());
        dto.setDescription(in.getDescription());
        dto.setStartTime(Instant.ofEpochSecond(in.getStart()));
        dto.setEndTime(Instant.ofEpochSecond(in.getEnd()));
        return dto;
    }
}
