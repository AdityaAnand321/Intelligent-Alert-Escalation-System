package com.example.demo.alerts.api;

import com.example.demo.alerts.model.Severity;
import com.example.demo.alerts.model.SourceType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CreateAlertRequest {
    private SourceType sourceType;
    private Severity severity;
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
