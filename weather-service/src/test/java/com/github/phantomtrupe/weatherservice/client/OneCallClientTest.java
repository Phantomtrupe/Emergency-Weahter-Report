package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.OneCallResponse;
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

class OneCallClientTest {

    @Test
    void shouldFetchOneCallAlertsCorrectly() throws Exception {
        // load JSON fixture
        String json = Files.readString(
            new ClassPathResource("fixtures/onecall.json").getFile().toPath(),
            StandardCharsets.UTF_8
        );

        // mock exchange function
        ExchangeFunction func = req -> Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build()
        );
        WebClient client = WebClient.builder().exchangeFunction(func).build();
        OneCallClient oneCallClient = new OneCallClient(client, "dummy-key");

        Mono<OneCallResponse> responseMono = oneCallClient.getOneCall(10.0, 20.0);

        StepVerifier.create(responseMono)
            .assertNext(resp -> {
                assert resp.getAlerts() != null;
                assert resp.getAlerts().size() == 2;
                assert resp.getAlerts().get(0).getEvent().equals("Storm");
                assert resp.getAlerts().get(1).getEvent().equals("Flood");
            })
            .verifyComplete();
    }
}
