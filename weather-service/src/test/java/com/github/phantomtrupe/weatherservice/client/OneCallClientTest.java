package com.github.phantomtrupe.weatherservice.client;

import com.github.phantomtrupe.weatherservice.model.OneCallResponse;
import com.github.phantomtrupe.weatherservice.model.OneCallAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OneCallClientTest {
    @Mock
    private WebClient webClient;
    @Mock
    private RequestHeadersUriSpec uriSpec;
    @Mock
    private RequestHeadersSpec headersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private OneCallClient oneCallClient;

    @BeforeEach
    void setUp() {
        oneCallClient = new OneCallClient(webClient, "api-key");
    }

    @Test
    void shouldFetchAlertsFromOneCallApi() {
        // given
        double lat = 1.23;
        double lon = 4.56;
        OneCallAlert alert1 = new OneCallAlert();
        alert1.setEvent("Event1");
        alert1.setDescription("Desc1");
        alert1.setStart(1000L);
        alert1.setEnd(2000L);
        OneCallAlert alert2 = new OneCallAlert();
        alert2.setEvent("Event2");
        alert2.setDescription("Desc2");
        alert2.setStart(3000L);
        alert2.setEnd(4000L);
        OneCallResponse response = new OneCallResponse();
        response.setAlerts(Arrays.asList(alert1, alert2));

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OneCallResponse.class)).thenReturn(Mono.just(response));

        // when
        Mono<OneCallResponse> result = oneCallClient.getOneCall(lat, lon);

        // then
        StepVerifier.create(result)
                .assertNext(resp -> {
                    assert resp.getAlerts().size() == 2;
                    assert resp.getAlerts().get(0).getEvent().equals("Event1");
                    assert resp.getAlerts().get(1).getEvent().equals("Event2");
                })
                .verifyComplete();
    }
}
