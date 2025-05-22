package com.github.phantomtrupe.weatherservice.service;

import com.github.phantomtrupe.weatherservice.client.AlertServiceClient;
import com.github.phantomtrupe.weatherservice.client.GeoClient;
import com.github.phantomtrupe.weatherservice.client.OneCallClient;
import com.github.phantomtrupe.weatherservice.client.UserServiceClient;
import com.github.phantomtrupe.weatherservice.model.GeocodeResponse;
import com.github.phantomtrupe.weatherservice.model.OneCallAlert;
import com.github.phantomtrupe.weatherservice.model.OneCallResponse;
import com.github.phantomtrupe.weatherservice.model.AlertDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import java.util.List;

@SpringBootTest
@AutoConfigureWebTestClient
class WeatherAlertServiceIntegrationTest {
    @Autowired
    private WeatherAlertService weatherAlertService;

    @MockBean
    private UserServiceClient userServiceClient;
    @MockBean
    private GeoClient geoClient;
    @MockBean
    private OneCallClient oneCallClient;
    @MockBean
    private AlertServiceClient alertServiceClient;

    @Test
    void shouldFetchAndForwardAlerts() {
        // given
        when(userServiceClient.getDistinctCities())
            .thenReturn(Flux.just("CityA", "CityB"));

        GeocodeResponse coordsA = new GeocodeResponse(); coordsA.setLat(10); coordsA.setLon(20);
        GeocodeResponse coordsB = new GeocodeResponse(); coordsB.setLat(30); coordsB.setLon(40);
        when(geoClient.geocode("CityA")).thenReturn(Flux.just(coordsA));
        when(geoClient.geocode("CityB")).thenReturn(Flux.just(coordsB));

        OneCallAlert alert1 = new OneCallAlert();
        alert1.setEvent("E1"); alert1.setDescription("D1"); alert1.setStart(100); alert1.setEnd(200);
        OneCallAlert alert2 = new OneCallAlert();
        alert2.setEvent("E2"); alert2.setDescription("D2"); alert2.setStart(300); alert2.setEnd(400);
        OneCallResponse respA = new OneCallResponse(); respA.setAlerts(List.of(alert1, alert2));
        OneCallResponse respB = new OneCallResponse(); respB.setAlerts(List.of());
        when(oneCallClient.getOneCall(10, 20)).thenReturn(Mono.just(respA));
        when(oneCallClient.getOneCall(30, 40)).thenReturn(Mono.just(respB));

        when(alertServiceClient.sendAlert(any(AlertDto.class))).thenReturn(Mono.empty());

        // when
        Flux<Void> result = weatherAlertService.processAlerts();

        // then
        StepVerifier.create(result)
            .expectComplete()
            .verify();

        ArgumentCaptor<AlertDto> captor = ArgumentCaptor.forClass(AlertDto.class);
        verify(alertServiceClient, times(2)).sendAlert(captor.capture());
        assert captor.getAllValues().stream().anyMatch(d -> d.getCity().equals("CityA") && d.getEvent().equals("E1"));
        assert captor.getAllValues().stream().anyMatch(d -> d.getCity().equals("CityA") && d.getEvent().equals("E2"));
    }

    @Test
    void shouldContinueOnGeocodeError() {
        // given
        when(userServiceClient.getDistinctCities())
            .thenReturn(Flux.just("BadCity", "GoodCity"));

        when(geoClient.geocode("BadCity")).thenReturn(Flux.error(new RuntimeException("fail")));
        GeocodeResponse good = new GeocodeResponse(); good.setLat(1); good.setLon(2);
        when(geoClient.geocode("GoodCity")).thenReturn(Flux.just(good));

        OneCallAlert alert = new OneCallAlert(); alert.setEvent("EG"); alert.setDescription("DG"); alert.setStart(50); alert.setEnd(60);
        OneCallResponse resp = new OneCallResponse(); resp.setAlerts(List.of(alert));
        when(oneCallClient.getOneCall(1, 2)).thenReturn(Mono.just(resp));

        when(alertServiceClient.sendAlert(any(AlertDto.class))).thenReturn(Mono.empty());

        // when
        Flux<Void> result = weatherAlertService.processAlerts();

        // then
        StepVerifier.create(result)
            .expectComplete()
            .verify();

        verify(alertServiceClient, times(1)).sendAlert(any(AlertDto.class));
    }
}
