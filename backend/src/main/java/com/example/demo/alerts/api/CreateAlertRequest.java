package com.example.demo.alerts.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.example.demo.alerts.model.Severity;
import com.example.demo.alerts.model.SourceType;

public class CreateAlertRequest {
    private SourceType sourceType;
    private Severity severity;
    private String driverId;
    private Instant timestamp;
    private Map<String, String> metadata = new HashMap<>();

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

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
