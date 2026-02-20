package com.example.demo.alerts.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.alerts.model.Alert;

public interface AlertRepository extends MongoRepository<Alert, String> {
}
