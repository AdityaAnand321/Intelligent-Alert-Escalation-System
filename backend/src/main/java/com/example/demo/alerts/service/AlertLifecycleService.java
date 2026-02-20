package com.example.demo.alerts.service;

import com.example.demo.alerts.model.AlertLifecycleEvent;
import com.example.demo.alerts.model.AlertStatus;
import com.example.demo.alerts.repo.AlertLifecycleEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertLifecycleService {

    private final AlertLifecycleEventRepository eventRepository;

    public AlertLifecycleService(AlertLifecycleEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public synchronized void addEvent(String alertId, String eventType, AlertStatus fromStatus, AlertStatus toStatus, String reason) {
        AlertLifecycleEvent event = new AlertLifecycleEvent();
        event.setAlertId(alertId);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setReason(reason);
        event.setTimestamp(Instant.now());
        eventRepository.save(event);
    }

    public synchronized List<AlertLifecycleEvent> allEvents() {
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(AlertLifecycleEvent::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public synchronized List<AlertLifecycleEvent> byAlertId(String alertId) {
        return eventRepository.findAll().stream()
                .filter(e -> e.getAlertId().equals(alertId))
                .sorted(Comparator.comparing(AlertLifecycleEvent::getTimestamp))
                .collect(Collectors.toList());
    }
}
