package com.example.demo.alerts.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.alerts.model.Alert;
import com.example.demo.alerts.model.AlertLifecycleEvent;
import com.example.demo.alerts.model.AlertStatus;
import com.example.demo.alerts.model.Severity;
import com.example.demo.alerts.rules.RuleDefinition;
import com.example.demo.alerts.rules.RuleProvider;

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

    /**
     * Counts alerts by severity level.
     * Time Complexity: O(n) where n = total alerts
     * Space Complexity: O(1) - fixed 3 severity types
     */
    public Map<String, Long> severityCounts() {
        Map<String, Long> counts = alertService.allAlerts().stream()
                .collect(Collectors.groupingBy(a -> a.getSeverity().name(), Collectors.counting()));

        return Map.of(
                Severity.CRITICAL.name(), counts.getOrDefault(Severity.CRITICAL.name(), 0L),
                Severity.WARNING.name(), counts.getOrDefault(Severity.WARNING.name(), 0L),
                Severity.INFO.name(), counts.getOrDefault(Severity.INFO.name(), 0L)
        );
    }

    /**
     * Returns top drivers with most open/escalated alerts.
     * Time Complexity: O(n log n) where n = total alerts (due to sorting)
     * Space Complexity: O(k) where k = unique drivers
     * Optimization: Use priority queue for O(n log limit) instead of O(n log n)
     */
    public List<Map<String, Object>> topDrivers(int limit) {
        return alertService.allAlerts().stream()
                .filter(a -> a.getStatus() == AlertStatus.OPEN || a.getStatus() == AlertStatus.ESCALATED)
                .collect(Collectors.groupingBy(a -> a.getMetadata().getOrDefault("driverId", "unknown"), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("driverId", entry.getKey());
                    map.put("openAlerts", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves recently auto-closed alerts within specified hours.
     * Time Complexity: O(n log n) where n = total alerts (includes sorting)
     * Space Complexity: O(k) where k = auto-closed alerts in time range
     */
    public List<Alert> recentAutoClosed(int hours) {
        Instant cutoff = Instant.now().minusSeconds(hours * 3600L);
        return alertService.allAlerts().stream()
                .filter(a -> a.getStatus() == AlertStatus.AUTO_CLOSED)
                .filter(a -> a.getUpdatedAt() != null && !a.getUpdatedAt().isBefore(cutoff))
                .sorted(Comparator.comparing(Alert::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns recent lifecycle events with enriched alert data.
     * Time Complexity: O(m log m + n) where m = total events, n = limit
     * Space Complexity: O(n) for returned events
     * Note: MongoDB sorts and limits server-side for efficiency
     */
    public List<Map<String, Object>> recentEvents(int limit) {
        return lifecycleService.allEvents().stream()
                .limit(limit)
                .map(event -> {
                    Alert alert = alertService.getIfPresent(event.getAlertId());
                    String sourceType = alert == null ? "UNKNOWN" : alert.getSourceType().name();
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("alertId", event.getAlertId());
                    map.put("eventType", event.getEventType());
                    map.put("timestamp", event.getTimestamp());
                    map.put("sourceType", sourceType);
                    map.put("toState", event.getToStatus());
                    map.put("reason", event.getReason() == null ? "" : event.getReason());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, RuleDefinition> activeRules() {
        return ruleProvider.allRules();
    }

    /**
     * Returns alert trend data grouped by date for the past N days.
     * Time Complexity: O(m) where m = events in time range
     * Space Complexity: O(d) where d = number of days
     * Optimization: Use MongoDB aggregation pipeline for server-side processing
     */
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
