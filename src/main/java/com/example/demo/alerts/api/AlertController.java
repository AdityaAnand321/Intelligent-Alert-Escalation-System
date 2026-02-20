package com.example.demo.alerts.api;

import com.example.demo.alerts.model.Alert;
import com.example.demo.alerts.model.AlertLifecycleEvent;
import com.example.demo.alerts.service.AlertLifecycleService;
import com.example.demo.alerts.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final AlertLifecycleService lifecycleService;

    public AlertController(AlertService alertService, AlertLifecycleService lifecycleService) {
        this.alertService = alertService;
        this.lifecycleService = lifecycleService;
    }

    @PostMapping
    public ResponseEntity<Alert> create(@RequestBody CreateAlertRequest request) {
        return ResponseEntity.ok(alertService.createAlert(request));
    }

    @GetMapping
    public ResponseEntity<List<Alert>> all() {
        return ResponseEntity.ok(alertService.allAlerts());
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String alertId) {
        return alertService.findById(alertId)
                .map(alert -> {
                    List<AlertLifecycleEvent> history = lifecycleService.byAlertId(alertId);
                    return ResponseEntity.ok(Map.of(
                            "alert", alert,
                            "history", history
                    ));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<Alert> resolve(@PathVariable String alertId) {
        return ResponseEntity.ok(alertService.resolve(alertId));
    }

    @PostMapping("/compliance-renewed")
    public ResponseEntity<Map<String, Object>> complianceRenewed(@RequestBody ComplianceRenewalRequest request) {
        int updated = alertService.markComplianceRenewed(request.getDriverId());
        return ResponseEntity.ok(Map.of(
                "driverId", request.getDriverId(),
                "updatedAlerts", updated,
                "message", "Compliance alerts marked as document_valid=true"
        ));
    }
}
