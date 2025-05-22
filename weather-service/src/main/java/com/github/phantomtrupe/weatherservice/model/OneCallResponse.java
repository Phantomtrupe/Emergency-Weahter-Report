package com.github.phantomtrupe.weatherservice.model;

import java.util.List;

public class OneCallResponse {
    private List<OneCallAlert> alerts;

    public List<OneCallAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<OneCallAlert> alerts) {
        this.alerts = alerts;
    }
}
