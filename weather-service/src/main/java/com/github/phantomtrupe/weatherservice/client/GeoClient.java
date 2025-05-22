package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.GeocodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class GeoClient {
    private final WebClient webClient;
    private final String apiKey;

    public GeoClient(WebClient geoWebClient, @Value("${openweathermap.api-key}") String apiKey) {
        this.webClient = geoWebClient;
        this.apiKey = apiKey;
    }

    public Flux<GeocodeResponse> geocode(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("q", city)
                        .queryParam("limit", 1)
                        .queryParam("appid", apiKey)
                        .build())
                .retrieve()
                .bodyToFlux(GeocodeResponse.class);
    }
}
