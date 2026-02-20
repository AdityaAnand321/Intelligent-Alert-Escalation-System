package com.example.demo.alerts.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.alerts.model.AlertLifecycleEvent;

public interface AlertLifecycleEventRepository extends MongoRepository<AlertLifecycleEvent, String> {
}
