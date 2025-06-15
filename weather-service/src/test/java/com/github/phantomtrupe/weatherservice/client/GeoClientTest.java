package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.GeocodeResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

class GeoClientTest {

    @Test
    void shouldGeocodeCityCorrectly() throws Exception {
        // load JSON fixture
        String json = Files.readString(
            new ClassPathResource("fixtures/geocode.json").getFile().toPath(),
            StandardCharsets.UTF_8
        );

        // mock exchange function to return fixture
        ExchangeFunction func = req -> Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build()
        );
        WebClient client = WebClient.builder().exchangeFunction(func).build();
        GeoClient geoClient = new GeoClient(client, "dummy-key");

        StepVerifier.create(geoClient.geocode("London").next())
            .expectNextMatches(resp -> resp.getLat() == 51.5074 && resp.getLon() == -0.1278)
            .verifyComplete();
    }
}
