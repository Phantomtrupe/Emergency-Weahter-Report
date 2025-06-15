package com.github.phantomtrupe.alertservice.service;

import com.github.phantomtrupe.alertservice.client.NotificationServiceClient;
import com.github.phantomtrupe.alertservice.client.UserServiceClient;
import com.github.phantomtrupe.alertservice.model.AlertRecord;
import com.github.phantomtrupe.alertservice.model.NotificationRequest;
import com.github.phantomtrupe.alertservice.repository.AlertRecordRepository;
import com.github.phantomtrupe.commons.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AlertProcessingServiceTest {
    @Mock
    private AlertRecordRepository repository;
    @Mock
    private UserServiceClient userClient;
    @Mock
    private NotificationServiceClient notificationClient;

    @InjectMocks
    private AlertProcessingService service;

    @Captor
    private ArgumentCaptor<NotificationRequest> requestCaptor;

    private com.github.phantomtrupe.commons.dto.AlertDto dto;
    private AlertRecord savedRecord;

    @BeforeEach
    void setUp() {
        dto = new com.github.phantomtrupe.commons.dto.AlertDto();
        dto.setCity("CityX");
        dto.setEvent("TestEvent");
        dto.setDescription("Desc");
        dto.setStartTime(Instant.ofEpochSecond(1000));
        dto.setEndTime(Instant.ofEpochSecond(2000));

        savedRecord = new AlertRecord();
        savedRecord.setCity(dto.getCity());
        savedRecord.setEvent(dto.getEvent());
        savedRecord.setDescription(dto.getDescription());
        savedRecord.setStartTime(dto.getStartTime());
        savedRecord.setEndTime(dto.getEndTime());
        savedRecord.setSentAt(Instant.now());
    }

    @Test
    void shouldSkipWhenDuplicateExists() {
        when(repository.existsByCityAndEventAndStartTimeAndEndTime(
                dto.getCity(), dto.getEvent(), dto.getStartTime(), dto.getEndTime()
        )).thenReturn(true);

        StepVerifier.create(service.processWeatherAlert(dto))
                .verifyComplete();

        verify(repository, never()).save(any());
        verify(userClient, never()).getUsersByCity(any());
        verify(notificationClient, never()).sendNotification(any());
    }

    @Test
    void shouldSaveAndNotifyUsers() {
        when(repository.existsByCityAndEventAndStartTimeAndEndTime(
                dto.getCity(), dto.getEvent(), dto.getStartTime(), dto.getEndTime()
        )).thenReturn(false);
        when(repository.save(any(AlertRecord.class))).thenReturn(savedRecord);
        UserDTO u1 = new UserDTO(); u1.setEmail("a@example.com"); u1.setPhoneNumber("111"); u1.setCity("CityX");
        UserDTO u2 = new UserDTO(); u2.setEmail("b@example.com"); u2.setPhoneNumber("222"); u2.setCity("CityX");
        when(userClient.getUsersByCity("CityX")).thenReturn(Flux.just(u1, u2));
        when(notificationClient.sendNotification(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.processWeatherAlert(dto))
                .verifyComplete();

        verify(repository).save(any(AlertRecord.class));
        verify(notificationClient, times(2)).sendNotification(requestCaptor.capture());
        List<NotificationRequest> sent = requestCaptor.getAllValues();
        assertThat(sent).extracting(NotificationRequest::getEmail)
                .containsExactlyInAnyOrder("a@example.com", "b@example.com");
        assertThat(sent).extracting(NotificationRequest::getPhoneNumber)
                .containsExactlyInAnyOrder("111", "222");
        assertThat(sent).allMatch(req -> req.getMessage().contains("[CityX] TestEvent"));
    }

    @Test
    void shouldContinueOnNotificationError() {
        when(repository.existsByCityAndEventAndStartTimeAndEndTime(
                dto.getCity(), dto.getEvent(), dto.getStartTime(), dto.getEndTime()
        )).thenReturn(false);
        when(repository.save(any(AlertRecord.class))).thenReturn(savedRecord);
        UserDTO user = new UserDTO(); user.setEmail("e@e"); user.setPhoneNumber("123"); user.setCity("CityX");
        when(userClient.getUsersByCity("CityX")).thenReturn(Flux.just(user));
        when(notificationClient.sendNotification(any()))
                .thenReturn(Mono.error(new RuntimeException("fail")));

        StepVerifier.create(service.processWeatherAlert(dto))
                .verifyComplete();

        verify(notificationClient).sendNotification(any());
    }
}

