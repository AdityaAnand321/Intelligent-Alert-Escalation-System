package com.example.demo.alerts.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.alerts.api.CreateAlertRequest;
import com.example.demo.alerts.model.Alert;
import com.example.demo.alerts.model.AlertStatus;
import com.example.demo.alerts.model.Severity;
import com.example.demo.alerts.model.SourceType;
import com.example.demo.alerts.repo.AlertRepository;

@Component
public class DemoAlertDataSeeder implements CommandLineRunner {

    private final AlertRepository alertRepository;
    private final AlertService alertService;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DemoAlertDataSeeder(AlertRepository alertRepository, AlertService alertService) {
        this.alertRepository = alertRepository;
        this.alertService = alertService;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || alertRepository.count() > 0) {
            return;
        }

        List<String> driverIds = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> String.format("DRV%03d", i))
                .collect(Collectors.toList());

        Random random = new Random(42);
        Instant now = Instant.now();

        for (int index = 0; index < driverIds.size(); index++) {
            String driverId = driverIds.get(index);
            String vehicleId = String.format("VEH%03d", index + 1);

            createAlert(driverId, vehicleId, SourceType.COMPLIANCE, Severity.WARNING,
                    now.minusSeconds((12L + random.nextInt(72)) * 3600L),
                    Map.of(
                        "document_valid", "false",
                        "document_expiry_date", now.minusSeconds((2L + random.nextInt(10)) * 24L * 3600L).toString(),
                        "reason", "Compliance document pending"
                    ));

            createAlert(driverId, vehicleId, SourceType.NEGATIVE_FEEDBACK, Severity.WARNING,
                    now.minusSeconds((2L + random.nextInt(36)) * 3600L),
                    Map.of("rating", String.valueOf(1 + random.nextInt(2)), "comment", "Rude behaviour complaint"));

            int overspeedCount = index < 4 ? 3 : 2;
            for (int j = 0; j < overspeedCount; j++) {
                createAlert(driverId, vehicleId, SourceType.OVERSPEEDING, Severity.WARNING,
                        now.minusSeconds((long) (j * 15 + random.nextInt(10)) * 60L),
                        Map.of("speed", String.valueOf(72 + random.nextInt(45)), "limit", "60"));
            }
        }

        alertService.markComplianceRenewed("DRV001");
        alertService.markComplianceRenewed("DRV004");
        alertService.markComplianceRenewed("DRV007");
        alertService.autoCloseEligible(1440);

        alertRepository.findAll().stream()
                .filter(alert -> alert.getStatus() == AlertStatus.OPEN)
                .limit(6)
                .forEach(alert -> alertService.resolve(alert.getAlertId()));
    }

    private Alert createAlert(String driverId, String vehicleId, SourceType sourceType, Severity severity,
            Instant timestamp, Map<String, String> extraMetadata) {
        CreateAlertRequest request = new CreateAlertRequest();
        request.setSourceType(sourceType);
        request.setSeverity(severity);
        request.setDriverId(driverId);
        request.setTimestamp(timestamp);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("driverId", driverId);
        metadata.put("vehicleId", vehicleId);
        metadata.putAll(extraMetadata);
        request.setMetadata(metadata);

        return alertService.createAlert(request);
    }
}
