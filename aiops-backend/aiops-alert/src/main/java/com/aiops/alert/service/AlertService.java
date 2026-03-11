package com.aiops.alert.service;

import com.aiops.alert.dto.CreateRuleRequest;
import com.aiops.alert.model.Alert;
import com.aiops.alert.model.AlertRule;
import com.aiops.alert.repository.AlertRepository;
import com.aiops.alert.repository.AlertRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AlertService {

    @Autowired
    private AlertRuleRepository ruleRepository;

    @Autowired
    private AlertRepository alertRepository;

    // AlertRule CRUD

    public List<AlertRule> findAllRules() {
        return ruleRepository.findAll();
    }

    public Optional<AlertRule> findRuleById(Long id) {
        return ruleRepository.findById(id);
    }

    @Transactional
    public AlertRule createRule(CreateRuleRequest request) {
        AlertRule rule = new AlertRule();
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setMetricName(request.getMetricName());
        rule.setOperator(request.getOperator());
        rule.setThreshold(request.getThreshold());
        rule.setDuration(request.getDuration() != null ? request.getDuration() : 0);
        rule.setEvalInterval(request.getEvalInterval() != null ? request.getEvalInterval() : 30);
        rule.setSeverity(AlertRule.AlertSeverity.valueOf(request.getSeverity().toUpperCase()));
        rule.setServerId(request.getServerId());
        rule.setServerGroup(request.getServerGroup());
        rule.setActionConfig(request.getActionConfig());
        rule.setStatus(AlertRule.AlertRuleStatus.ENABLED);
        
        return ruleRepository.save(rule);
    }

    @Transactional
    public AlertRule updateRule(Long id, CreateRuleRequest request) {
        AlertRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        
        if (request.getName() != null) rule.setName(request.getName());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        if (request.getMetricName() != null) rule.setMetricName(request.getMetricName());
        if (request.getOperator() != null) rule.setOperator(request.getOperator());
        if (request.getThreshold() != null) rule.setThreshold(request.getThreshold());
        if (request.getSeverity() != null) rule.setSeverity(AlertRule.AlertSeverity.valueOf(request.getSeverity().toUpperCase()));
        
        rule.setUpdatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    @Transactional
    public AlertRule enableRule(Long id) {
        AlertRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        rule.setStatus(AlertRule.AlertRuleStatus.ENABLED);
        rule.setUpdatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    @Transactional
    public AlertRule disableRule(Long id) {
        AlertRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        rule.setStatus(AlertRule.AlertRuleStatus.DISABLED);
        rule.setUpdatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    // Alert CRUD

    public List<Alert> findAllAlerts() {
        return alertRepository.findAll();
    }

    public List<Alert> findAlertsByStatus(Alert.AlertStatus status) {
        return alertRepository.findByStatusOrderByFiredAtDesc(status);
    }

    public List<Alert> findFiringAlerts() {
        return alertRepository.findByStatusOrderByFiredAtDesc(Alert.AlertStatus.FIRING);
    }

    @Transactional
    public Alert acknowledgeAlert(Long id, Long userId) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
        
        alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(userId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert resolveAlert(Long id, Long userId, String resolution) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
        
        alert.setStatus(Alert.AlertStatus.RESOLVED);
        alert.setResolvedBy(userId);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolution(resolution);
        
        return alertRepository.save(alert);
    }

    // Rule Engine

    public void evaluate(Long serverId, String metricName, Double value) {
        List<AlertRule> rules = ruleRepository.findByStatus(AlertRule.AlertRuleStatus.ENABLED);
        
        for (AlertRule rule : rules) {
            // Check if rule applies to this server/metric
            if (!matchesServer(rule, serverId)) continue;
            if (!rule.getMetricName().equals(metricName)) continue;
            
            // Evaluate condition
            if (evaluateCondition(rule, value)) {
                // Trigger alert
                triggerAlert(rule, serverId, metricName, value);
            }
        }
    }

    private boolean matchesServer(AlertRule rule, Long serverId) {
        if (rule.getServerId() != null && rule.getServerId() != serverId) {
            return false;
        }
        return true;
    }

    private boolean evaluateCondition(AlertRule rule, Double value) {
        String op = rule.getOperator();
        Double threshold = rule.getThreshold();
        
        return switch (op) {
            case ">" -> value > threshold;
            case ">=" -> value >= threshold;
            case "<" -> value < threshold;
            case "<=" -> value <= threshold;
            case "==" -> Math.abs(value - threshold) < 0.01;
            case "!=" -> Math.abs(value - threshold) >= 0.01;
            default -> false;
        };
    }

    @Transactional
    protected void triggerAlert(AlertRule rule, Long serverId, String metricName, Double value) {
        // Check if already firing
        List<Alert> existing = alertRepository.findByRuleIdAndStatus(rule.getId(), Alert.AlertStatus.FIRING);
        if (!existing.isEmpty()) return;
        
        Alert alert = new Alert();
        alert.setRuleId(rule.getId());
        alert.setRuleName(rule.getName());
        alert.setServerId(serverId);
        alert.setMetricName(metricName);
        alert.setValue(value);
        alert.setThreshold(rule.getThreshold());
        alert.setSeverity(convertSeverity(rule.getSeverity()));
        alert.setMessage(String.format("%s: %s %.1f %s %.1f", 
                rule.getName(), metricName, value, rule.getOperator(), rule.getThreshold()));
        alert.setStatus(Alert.AlertStatus.FIRING);
        
        alertRepository.save(alert);
    }

    private Alert.AlertSeverity convertSeverity(AlertRule.AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> Alert.AlertSeverity.CRITICAL;
            case WARNING -> Alert.AlertSeverity.WARNING;
            case INFO -> Alert.AlertSeverity.INFO;
        };
    }
}
