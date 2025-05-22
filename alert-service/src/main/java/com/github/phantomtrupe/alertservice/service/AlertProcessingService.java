package com.github.phantomtrupe.alertservice.service;

import com.github.phantomtrupe.alertservice.model.AlertDto;
import com.github.phantomtrupe.alertservice.model.AlertRecord;
import com.github.phantomtrupe.alertservice.model.NotificationRequest;
import com.github.phantomtrupe.alertservice.repository.AlertRecordRepository;
import com.github.phantomtrupe.alertservice.client.UserServiceClient;
import com.github.phantomtrupe.alertservice.client.NotificationServiceClient;
import com.github.phantomtrupe.commons.dto.UserDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;

@Service
public class AlertProcessingService {
    private final AlertRecordRepository repository;
    private final UserServiceClient userClient;
    private final NotificationServiceClient notificationClient;

    public AlertProcessingService(AlertRecordRepository repository,
                                  UserServiceClient userClient,
                                  NotificationServiceClient notificationClient) {
        this.repository = repository;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
    }

    public Mono<Void> processWeatherAlert(AlertDto dto) {
        return Mono.fromCallable(() -> repository.existsByCityAndEventAndStartTimeAndEndTime(
                dto.getCity(), dto.getEvent(), dto.getStartTime(), dto.getEndTime()))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.empty();
                }
                // persist record
                AlertRecord record = new AlertRecord();
                record.setCity(dto.getCity());
                record.setEvent(dto.getEvent());
                record.setDescription(dto.getDescription());
                record.setStartTime(dto.getStartTime());
                record.setEndTime(dto.getEndTime());
                record.setSentAt(Instant.now());
                return Mono.fromCallable(() -> repository.save(record))
                        .subscribeOn(Schedulers.boundedElastic())
                        .then(Mono.just(record));
            })
            .flatMapMany(record -> userClient.getUsersByCityAndSeverity(record.getCity(), record.getEvent()))
            .flatMap(user -> {
                NotificationRequest req = new NotificationRequest();
                req.setEmail(user.getEmail());
                req.setPhoneNumber(user.getPhoneNumber());
                req.setMessage(String.format("[%s] %s (from %s to %s)",
                        user.getCity(), dto.getEvent(), dto.getStartTime(), dto.getEndTime()));
                return notificationClient.sendNotification(req);
            })
            .then();
    }

    public Flux<AlertRecord> getAlertHistory() {
        return Mono.fromCallable(() -> repository.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }
}
