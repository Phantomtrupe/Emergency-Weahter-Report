package com.github.phantomtrupe.weatherservice.service;

import com.github.phantomtrupe.commons.dto.AlertDto;
import com.github.phantomtrupe.weatherservice.client.AlertServiceClient;
import com.github.phantomtrupe.weatherservice.client.GeoClient;
import com.github.phantomtrupe.weatherservice.client.OneCallClient;
import com.github.phantomtrupe.weatherservice.client.UserServiceClient;
import com.github.phantomtrupe.weatherservice.model.GeocodeResponse;
import com.github.phantomtrupe.weatherservice.model.OneCallAlert;
import com.github.phantomtrupe.weatherservice.model.OneCallResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private GeoClient geoClient;
    @Mock
    private OneCallClient oneCallClient;
    @Mock
    private AlertServiceClient alertServiceClient;

    @InjectMocks
    private WeatherAlertService weatherAlertService;

    @Captor
    private ArgumentCaptor<AlertDto> alertCaptor;

    private GeocodeResponse geoA;
    private GeocodeResponse geoB;
    private OneCallResponse responseA;
    private OneCallResponse responseB;

    @BeforeEach
    void setUpData() {
        geoA = new GeocodeResponse();
        geoA.setLat(10.0);
        geoA.setLon(20.0);
        geoB = new GeocodeResponse();
        geoB.setLat(30.0);
        geoB.setLon(40.0);

        OneCallAlert a1 = new OneCallAlert();
        a1.setEvent("Storm");
        a1.setDescription("Heavy storm");
        a1.setStart(1000);
        a1.setEnd(2000);
        OneCallAlert a2 = new OneCallAlert();
        a2.setEvent("Flood");
        a2.setDescription("Flood warning");
        a2.setStart(3000);
        a2.setEnd(4000);
        responseA = new OneCallResponse();
        responseA.setAlerts(List.of(a1, a2));

        responseB = new OneCallResponse();
        responseB.setAlerts(List.of());
    }

    @Test
    void shouldForwardAlertsAndCompleteMono() {
        when(userServiceClient.getDistinctCities()).thenReturn(Flux.just("CityA", "CityB"));
        when(geoClient.geocode("CityA")).thenReturn(Flux.just(geoA));
        when(geoClient.geocode("CityB")).thenReturn(Flux.just(geoB));
        when(oneCallClient.getOneCall(10.0, 20.0)).thenReturn(Mono.just(responseA));
        when(oneCallClient.getOneCall(30.0, 40.0)).thenReturn(Mono.just(responseB));
        when(alertServiceClient.sendAlert(any())).thenReturn(Mono.empty());

        StepVerifier.create(weatherAlertService.processAlerts())
            .verifyComplete();

        verify(alertServiceClient, times(2)).sendAlert(alertCaptor.capture());
        List<AlertDto> sent = alertCaptor.getAllValues();
        assertThat(sent).extracting(AlertDto::getCity)
            .containsExactlyInAnyOrder("CityA", "CityA");
        assertThat(sent).extracting(AlertDto::getEvent)
            .containsExactlyInAnyOrder("Storm", "Flood");
        // verify startTime and endTime epoch seconds match input values
        assertThat(sent).extracting(AlertDto::getStartTime)
            .extracting(Instant::getEpochSecond)
            .containsExactlyInAnyOrder(1000L, 3000L);
        assertThat(sent).extracting(AlertDto::getEndTime)
            .extracting(Instant::getEpochSecond)
            .containsExactlyInAnyOrder(2000L, 4000L);
    }

    @Test
    void shouldSkipGeocodeErrorAndContinueProcessing() {
        when(userServiceClient.getDistinctCities()).thenReturn(Flux.just("BadCity", "GoodCity"));
        when(geoClient.geocode("BadCity")).thenReturn(Flux.error(new RuntimeException("geo error")));
        when(geoClient.geocode("GoodCity")).thenReturn(Flux.just(geoB));
        when(oneCallClient.getOneCall(30.0, 40.0)).thenReturn(Mono.just(responseA));
        when(alertServiceClient.sendAlert(any())).thenReturn(Mono.empty());

        StepVerifier.create(weatherAlertService.processAlerts())
            .verifyComplete();

        verify(alertServiceClient, times(2)).sendAlert(alertCaptor.capture());
        // all alerts from responseA only
        List<AlertDto> sent = alertCaptor.getAllValues();
        assertThat(sent).allMatch(dto -> dto.getCity().equals("GoodCity"));  }
}
