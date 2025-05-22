package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.OneCallResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OneCallClient {
    private final WebClient webClient;
    private final String apiKey;

    public OneCallClient(WebClient onecallWebClient, @Value("${openweathermap.api-key}") String apiKey) {
        this.webClient = onecallWebClient;
        this.apiKey = apiKey;
    }

    public Mono<OneCallResponse> getOneCall(double lat, double lon) {
        return webClient.get()
                .uri(uri -> uri
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("exclude", "minutely,hourly,daily")
                        .queryParam("appid", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(OneCallResponse.class);
    }
}
