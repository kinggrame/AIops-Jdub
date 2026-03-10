package com.aiops.alert.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "operator", nullable = false)
    private String operator;

    @Column(name = "threshold", nullable = false)
    private Double threshold;

    @Column(name = "duration")
    private Integer duration = 0;

    @Column(name = "eval_interval")
    private Integer evalInterval = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity = AlertSeverity.WARNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertRuleStatus status = AlertRuleStatus.ENABLED;

    @Column(name = "server_id")
    private Long serverId;

    @Column(name = "server_group")
    private String serverGroup;

    @Column(name = "action_config")
    private String actionConfig;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AlertRule() {
        this.createdAt = LocalDateTime.now();
    }

    public enum AlertSeverity {
        CRITICAL,
        WARNING,
        INFO
    }

    public enum AlertRuleStatus {
        ENABLED,
        DISABLED
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getEvalInterval() { return evalInterval; }
    public void setEvalInterval(Integer evalInterval) { this.evalInterval = evalInterval; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public AlertRuleStatus getStatus() { return status; }
    public void setStatus(AlertRuleStatus status) { this.status = status; }

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public String getServerGroup() { return serverGroup; }
    public void setServerGroup(String serverGroup) { this.serverGroup = serverGroup; }

    public String getActionConfig() { return actionConfig; }
    public void setActionConfig(String actionConfig) { this.actionConfig = actionConfig; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
