package com.aiops.alert.controller;

import com.aiops.alert.dto.CreateRuleRequest;
import com.aiops.alert.model.Alert;
import com.aiops.alert.model.AlertRule;
import com.aiops.alert.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // Alert Rules

    @GetMapping("/alert/rules")
    public ResponseEntity<List<AlertRule>> listRules() {
        return ResponseEntity.ok(alertService.findAllRules());
    }

    @GetMapping("/alert/rules/{id}")
    public ResponseEntity<AlertRule> getRule(@PathVariable Long id) {
        return alertService.findRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/alert/rules")
    public ResponseEntity<AlertRule> createRule(@RequestBody CreateRuleRequest request) {
        AlertRule rule = alertService.createRule(request);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/alert/rules/{id}")
    public ResponseEntity<AlertRule> updateRule(@PathVariable Long id, @RequestBody CreateRuleRequest request) {
        AlertRule rule = alertService.updateRule(id, request);
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/alert/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alert/rules/{id}/enable")
    public ResponseEntity<AlertRule> enableRule(@PathVariable Long id) {
        AlertRule rule = alertService.enableRule(id);
        return ResponseEntity.ok(rule);
    }

    @PostMapping("/alert/rules/{id}/disable")
    public ResponseEntity<AlertRule> disableRule(@PathVariable Long id) {
        AlertRule rule = alertService.disableRule(id);
        return ResponseEntity.ok(rule);
    }

    // Alerts

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> listAlerts(@RequestParam(required = false) String status) {
        List<Alert> alerts;
        if (status != null && !status.isEmpty()) {
            alerts = alertService.findAlertsByStatus(Alert.AlertStatus.valueOf(status.toUpperCase()));
        } else {
            alerts = alertService.findAllAlerts();
        }
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/alerts/firing")
    public ResponseEntity<List<Alert>> listFiringAlerts() {
        return ResponseEntity.ok(alertService.findFiringAlerts());
    }

    @PostMapping("/alerts/{id}/ack")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id) {
        Alert alert = alertService.acknowledgeAlert(id, 1L);
        return ResponseEntity.ok(alert);
    }

    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id, @RequestBody(required = false) String resolution) {
        Alert alert = alertService.resolveAlert(id, 1L, resolution != null ? resolution : "");
        return ResponseEntity.ok(alert);
    }
}
