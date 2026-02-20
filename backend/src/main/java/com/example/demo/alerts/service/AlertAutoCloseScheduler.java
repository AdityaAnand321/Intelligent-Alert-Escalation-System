package com.example.demo.alerts.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertAutoCloseScheduler {

    private final AlertService alertService;

    @Value("${alerts.expiry-mins:1440}")
    private long expiryMins;

    public AlertAutoCloseScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(fixedDelayString = "${alerts.scheduler.fixed-delay-ms:120000}")
    public void scanAndAutoClose() {
        alertService.autoCloseEligible(expiryMins);
    }
}
