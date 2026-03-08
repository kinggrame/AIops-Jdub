package com.aiops.api.controller;

import com.aiops.api.dto.request.CreateAlertRequest;
import com.aiops.common.model.ApiResponse;
import com.aiops.detection.entity.Alert;
import com.aiops.detection.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(alertService.list());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateAlertRequest request) {
        Alert alert = new Alert(
                UUID.randomUUID().toString(),
                request.hostname(),
                request.source(),
                request.metric(),
                request.severity(),
                request.currentValue(),
                request.threshold(),
                request.description(),
                request.status() == null || request.status().isBlank() ? "open" : request.status(),
                Instant.now()
        );
        return ApiResponse.ok(alertService.create(alert), "Alert created");
    }
}
