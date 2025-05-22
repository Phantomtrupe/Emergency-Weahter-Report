package com.github.phantomtrupe.weatherservice.model;

public class OneCallAlert {
    private String event;
    private String description;
    private long start;
    private long end;

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getStart() { return start; }
    public void setStart(long start) { this.start = start; }

    public long getEnd() { return end; }
    public void setEnd(long end) { this.end = end; }
}
