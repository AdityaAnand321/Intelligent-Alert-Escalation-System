package com.example.demo.alerts.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.alerts.api.CreateAlertRequest;
import com.example.demo.alerts.model.Alert;
import com.example.demo.alerts.model.AlertStatus;
import com.example.demo.alerts.model.Severity;
import com.example.demo.alerts.model.SourceType;
import com.example.demo.alerts.repo.AlertRepository;
import com.example.demo.alerts.rules.RuleDefinition;
import com.example.demo.alerts.rules.RuleProvider;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final RuleProvider ruleProvider;
    private final AlertLifecycleService lifecycleService;

    public AlertService(AlertRepository alertRepository, RuleProvider ruleProvider, AlertLifecycleService lifecycleService) {
        this.alertRepository = alertRepository;
        this.ruleProvider = ruleProvider;
        this.lifecycleService = lifecycleService;
    }

    /**
     * Creates a new alert and evaluates escalation rules.
     * Time Complexity: O(n) where n = alerts in rule evaluation window
     * Space Complexity: O(1) - single alert storage
     */
    public Alert createAlert(CreateAlertRequest request) {
        if (request.getSourceType() == null) {
            throw new IllegalArgumentException("sourceType is required");
        }

        Alert alert = new Alert();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setSourceType(request.getSourceType());
        alert.setSeverity(request.getSeverity() == null ? Severity.WARNING : request.getSeverity());
        alert.setTimestamp(request.getTimestamp() == null ? Instant.now() : request.getTimestamp());
        alert.setStatus(AlertStatus.OPEN);
        alert.setMetadata(request.getMetadata() == null ? new HashMap<>() : new HashMap<>(request.getMetadata()));
        alert.setUpdatedAt(Instant.now());

        alertRepository.save(alert);
        lifecycleService.addEvent(alert.getAlertId(), "CREATED", null, AlertStatus.OPEN, "Alert created");

        evaluateEscalation(alert);
        return alert;
    }

    /**
     * Retrieves all alerts sorted by timestamp.
     * Time Complexity: O(n log n) where n = total alerts (sorting)
     * Space Complexity: O(n) - list of all alerts
     * Recommendation: Add pagination for production (limit/offset)
     */
    public List<Alert> allAlerts() {
        return alertRepository.findAll().stream()
                .sorted(Comparator.comparing(Alert::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds alert by ID using MongoDB indexed lookup.
     * Time Complexity: O(1) average case with index
     * Space Complexity: O(1)
     */
    public Optional<Alert> findById(String alertId) {
        return alertRepository.findById(alertId);
    }

    public Alert getIfPresent(String alertId) {
        return alertRepository.findById(alertId).orElse(null);
    }

    public Alert resolve(String alertId) {
        Alert alert = requiredAlert(alertId);
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            return alert;
        }

        AlertStatus from = alert.getStatus();
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setUpdatedAt(Instant.now());
        alertRepository.save(alert);
        lifecycleService.addEvent(alertId, "RESOLVED", from, AlertStatus.RESOLVED, "Manually resolved");
        return alert;
    }

    /**
     * Marks compliance alerts as renewed for a driver.
     * Time Complexity: O(n) where n = total alerts (linear scan)
     * Space Complexity: O(1)
     * Optimization: Add compound index on (sourceType, metadata.driverId, status)
     */
    public int markComplianceRenewed(String driverId) {
        int updated = 0;
        for (Alert alert : alertRepository.findAll()) {
            if (alert.getSourceType() == SourceType.COMPLIANCE
                    && isOpenLike(alert)
                    && driverId != null
                    && driverId.equals(alert.getMetadata().get("driverId"))) {
                alert.getMetadata().put("document_valid", "true");
                alert.setUpdatedAt(Instant.now());
                alertRepository.save(alert);
                updated++;
            }
        }
        return updated;
    }

    /**
     * Finds and auto-closes eligible alerts based on rules and time expiry.
     * Time Complexity: O(n) where n = total alerts
     * Space Complexity: O(k) where k = closed alerts
     * Optimization: Filter at database level with autoCloseReason != null
     */
    public List<Alert> autoCloseEligible(long expiryMins) {
        Instant now = Instant.now();
        List<Alert> closed = new ArrayList<>();

        for (Alert alert : alertRepository.findAll()) {
            if (!isOpenLike(alert)) {
                continue;
            }

            String reason = null;
            RuleDefinition rule = ruleProvider.forSource(alert.getSourceType());

            if (rule != null && "document_valid".equalsIgnoreCase(rule.getAutoCloseIf())) {
                String valid = alert.getMetadata().getOrDefault("document_valid", "false");
                if ("true".equalsIgnoreCase(valid)) {
                    reason = "Document renewed";
                }
            }

            if (reason == null && expiryMins > 0) {
                long age = Duration.between(alert.getTimestamp(), now).toMinutes();
                if (age >= expiryMins) {
                    reason = "Time window expired";
                }
            }

            if (reason != null) {
                AlertStatus from = alert.getStatus();
                alert.setStatus(AlertStatus.AUTO_CLOSED);
                alert.setAutoCloseReason(reason);
                alert.setUpdatedAt(Instant.now());
                alertRepository.save(alert);
                lifecycleService.addEvent(alert.getAlertId(), "AUTO_CLOSED", from, AlertStatus.AUTO_CLOSED, reason);
                closed.add(alert);
            }
        }

        return closed;
    }

    /**
     * Evaluates if alert should be escalated based on threshold rules.
     * Time Complexity: O(n) where n = alerts in time window
     * Space Complexity: O(1)
     * Example: 3 overspeed alerts in 60 minutes triggers escalation
     */
    private void evaluateEscalation(Alert alert) {
        RuleDefinition rule = ruleProvider.forSource(alert.getSourceType());
        if (rule == null || rule.getEscalateIfCount() == null || rule.getWindowMins() == null || alert.isEscalationTriggered()) {
            return;
        }

        String driverId = alert.getMetadata().get("driverId");
        if (driverId == null || driverId.isBlank()) {
            return;
        }

        Instant cutoff = alert.getTimestamp().minus(Duration.ofMinutes(rule.getWindowMins()));
        long count = alertRepository.findAll().stream()
                .filter(a -> a.getSourceType() == alert.getSourceType())
                .filter(a -> driverId.equals(a.getMetadata().get("driverId")))
                .filter(a -> !a.getTimestamp().isBefore(cutoff))
                .count();

        if (count >= rule.getEscalateIfCount()) {
            AlertStatus from = alert.getStatus();
            alert.setStatus(AlertStatus.ESCALATED);
            alert.setSeverity(Severity.CRITICAL);
            alert.setEscalationTriggered(true);
            alert.setUpdatedAt(Instant.now());
            alertRepository.save(alert);
            lifecycleService.addEvent(alert.getAlertId(), "ESCALATED", from, AlertStatus.ESCALATED,
                    "Rule triggered: count=" + count + " in " + rule.getWindowMins() + " mins");
        }
    }

    private Alert requiredAlert(String alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
    }

    private boolean isOpenLike(Alert alert) {
        return alert.getStatus() == AlertStatus.OPEN || alert.getStatus() == AlertStatus.ESCALATED;
    }
}
