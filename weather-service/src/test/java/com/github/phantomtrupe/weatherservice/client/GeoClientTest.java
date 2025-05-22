package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.GeocodeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoClientTest {
    @Mock
    private WebClient webClient;
    @Mock
    private RequestHeadersUriSpec uriSpec;
    @Mock
    private RequestHeadersSpec headersSpec;
    @Mock
    private ResponseSpec responseSpec;

    private GeoClient geoClient;

    @BeforeEach
    void setUp() {
        geoClient = new GeoClient(webClient, "test-key");
    }

    @Test
    void shouldGeocodeCityToCoords() {
        // given
        String city = "TestCity";
        GeocodeResponse expected = new GeocodeResponse();
        expected.setLat(12.34);
        expected.setLon(56.78);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GeocodeResponse.class))
                .thenReturn(Flux.just(expected));

        // when
        Flux<GeocodeResponse> result = geoClient.geocode(city);

        // then
        StepVerifier.create(result)
                .expectNextMatches(g -> g.getLat() == 12.34 && g.getLon() == 56.78)
                .verifyComplete();
    }
}
