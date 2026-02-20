package com.example.demo.alerts.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.alerts.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(Map.of(
                "severityCounts", dashboardService.severityCounts(),
                "topDrivers", dashboardService.topDrivers(5),
                "recentEvents", dashboardService.recentEvents(20),
                "recentAutoClosed", dashboardService.recentAutoClosed(24)
        ));
    }

    @GetMapping("/top-offenders")
    public ResponseEntity<Object> topOffenders(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.topDrivers(limit));
    }

    @GetMapping("/events")
    public ResponseEntity<Object> events(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(dashboardService.recentEvents(limit));
    }

    @GetMapping("/auto-closed")
    public ResponseEntity<Object> autoClosed(@RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(dashboardService.recentAutoClosed(hours));
    }

    @GetMapping("/trend")
    public ResponseEntity<Object> trend(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(dashboardService.trend(days));
    }

    @GetMapping("/config/rules")
    public ResponseEntity<Object> activeRules() {
        return ResponseEntity.ok(dashboardService.activeRules());
    }
}
