package com.example.demo.alerts.model;

import java.time.Instant;

public class AlertLifecycleEvent {
    private String alertId;
    private String eventType;
    private AlertStatus fromStatus;
    private AlertStatus toStatus;
    private String reason;
    private Instant timestamp;

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public AlertStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(AlertStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public AlertStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(AlertStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
