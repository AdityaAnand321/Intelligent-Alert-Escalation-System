package com.example.demo.alerts.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "alerts")
public class Alert {
    @Id
    private String alertId;
    private SourceType sourceType;
    private Severity severity;
    private Instant timestamp;
    private AlertStatus status;
    private Map<String, String> metadata = new HashMap<>();
    private boolean escalationTriggered;
    private String autoCloseReason;
    private Instant updatedAt;

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public boolean isEscalationTriggered() {
        return escalationTriggered;
    }

    public void setEscalationTriggered(boolean escalationTriggered) {
        this.escalationTriggered = escalationTriggered;
    }

    public String getAutoCloseReason() {
        return autoCloseReason;
    }

    public void setAutoCloseReason(String autoCloseReason) {
        this.autoCloseReason = autoCloseReason;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
