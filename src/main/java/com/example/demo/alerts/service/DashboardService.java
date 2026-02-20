package com.example.demo.alerts.service;

import com.example.demo.alerts.model.Alert;
import com.example.demo.alerts.model.AlertLifecycleEvent;
import com.example.demo.alerts.model.AlertStatus;
import com.example.demo.alerts.model.Severity;
import com.example.demo.alerts.rules.RuleDefinition;
import com.example.demo.alerts.rules.RuleProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AlertService alertService;
    private final AlertLifecycleService lifecycleService;
    private final RuleProvider ruleProvider;

    public DashboardService(AlertService alertService, AlertLifecycleService lifecycleService, RuleProvider ruleProvider) {
        this.alertService = alertService;
        this.lifecycleService = lifecycleService;
        this.ruleProvider = ruleProvider;
    }

    public Map<String, Long> severityCounts() {
        Map<String, Long> counts = alertService.allAlerts().stream()
                .collect(Collectors.groupingBy(a -> a.getSeverity().name(), Collectors.counting()));

        return Map.of(
                Severity.CRITICAL.name(), counts.getOrDefault(Severity.CRITICAL.name(), 0L),
                Severity.WARNING.name(), counts.getOrDefault(Severity.WARNING.name(), 0L),
                Severity.INFO.name(), counts.getOrDefault(Severity.INFO.name(), 0L)
        );
    }

    public List<Map<String, Object>> topDrivers(int limit) {
        return alertService.allAlerts().stream()
                .filter(a -> a.getStatus() == AlertStatus.OPEN || a.getStatus() == AlertStatus.ESCALATED)
                .collect(Collectors.groupingBy(a -> a.getMetadata().getOrDefault("driverId", "unknown"), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> Map.of("driverId", entry.getKey(), "openAlerts", entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<Alert> recentAutoClosed(int hours) {
        Instant cutoff = Instant.now().minusSeconds(hours * 3600L);
        return alertService.allAlerts().stream()
                .filter(a -> a.getStatus() == AlertStatus.AUTO_CLOSED)
                .filter(a -> a.getUpdatedAt() != null && !a.getUpdatedAt().isBefore(cutoff))
                .sorted(Comparator.comparing(Alert::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> recentEvents(int limit) {
        return lifecycleService.allEvents().stream()
                .limit(limit)
                .map(event -> {
                    Alert alert = alertService.getIfPresent(event.getAlertId());
                    String sourceType = alert == null ? "UNKNOWN" : alert.getSourceType().name();
                    return Map.of(
                            "alertId", event.getAlertId(),
                            "eventType", event.getEventType(),
                            "timestamp", event.getTimestamp(),
                            "sourceType", sourceType,
                            "toState", event.getToStatus(),
                            "reason", event.getReason() == null ? "" : event.getReason()
                    );
                })
                .collect(Collectors.toList());
    }

    public Map<String, RuleDefinition> activeRules() {
        return ruleProvider.allRules();
    }

    public List<Map<String, Object>> trend(int days) {
        Instant cutoff = Instant.now().minusSeconds(days * 24L * 3600L);

        Map<LocalDate, List<AlertLifecycleEvent>> byDate = lifecycleService.allEvents().stream()
                .filter(e -> !e.getTimestamp().isBefore(cutoff))
                .collect(Collectors.groupingBy(e -> e.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate()));

        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    long created = entry.getValue().stream().filter(e -> "CREATED".equals(e.getEventType())).count();
                    long escalated = entry.getValue().stream().filter(e -> "ESCALATED".equals(e.getEventType())).count();
                    long autoClosed = entry.getValue().stream().filter(e -> "AUTO_CLOSED".equals(e.getEventType())).count();

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", entry.getKey().toString());
                    row.put("totalAlerts", created);
                    row.put("escalations", escalated);
                    row.put("autoClosures", autoClosed);
                    return row;
                })
                .collect(Collectors.toList());
    }
}
