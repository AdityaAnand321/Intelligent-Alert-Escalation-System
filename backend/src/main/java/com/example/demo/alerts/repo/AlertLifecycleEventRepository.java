package com.example.demo.alerts.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.alerts.model.AlertLifecycleEvent;

public interface AlertLifecycleEventRepository extends JpaRepository<AlertLifecycleEvent, String> {
}
