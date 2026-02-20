package com.example.demo.alerts.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.alerts.model.Alert;

public interface AlertRepository extends JpaRepository<Alert, String> {
}
