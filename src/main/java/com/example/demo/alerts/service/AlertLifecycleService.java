package com.example.demo.alerts.service;

import com.example.demo.alerts.model.AlertLifecycleEvent;
import com.example.demo.alerts.model.AlertStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertLifecycleService {

    private final List<AlertLifecycleEvent> events = new ArrayList<>();

    public synchronized void addEvent(String alertId, String eventType, AlertStatus fromStatus, AlertStatus toStatus, String reason) {
        AlertLifecycleEvent event = new AlertLifecycleEvent();
        event.setAlertId(alertId);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setReason(reason);
        event.setTimestamp(Instant.now());
        events.add(event);
    }

    public synchronized List<AlertLifecycleEvent> allEvents() {
        return events.stream()
                .sorted(Comparator.comparing(AlertLifecycleEvent::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public synchronized List<AlertLifecycleEvent> byAlertId(String alertId) {
        return events.stream()
                .filter(e -> e.getAlertId().equals(alertId))
                .sorted(Comparator.comparing(AlertLifecycleEvent::getTimestamp))
                .collect(Collectors.toList());
    }
}
