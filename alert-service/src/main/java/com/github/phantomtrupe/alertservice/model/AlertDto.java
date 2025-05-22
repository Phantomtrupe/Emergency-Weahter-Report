package com.github.phantomtrupe.alertservice.model;

import java.time.Instant;

public class AlertDto {
    private String city;
    private String event;
    private String description;
    private Instant startTime;
    private Instant endTime;

    // Getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
