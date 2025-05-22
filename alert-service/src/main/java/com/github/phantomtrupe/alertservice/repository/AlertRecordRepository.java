package com.github.phantomtrupe.alertservice.repository;

import com.github.phantomtrupe.alertservice.model.AlertRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long> {
    boolean existsByCityAndEventAndStartTimeAndEndTime(String city, String event, Instant startTime, Instant endTime);
}
