package com.aiops.action.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_executions")
public class ActionExecution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long actionId;
    private String actionName;
    private Long ruleId;
    private Long alertId;
    private Long serverId;
    @Enumerated(EnumType.STRING) private ExecutionStatus status = ExecutionStatus.PENDING;
    @Column(columnDefinition = "TEXT") private String input;
    @Column(columnDefinition = "TEXT") private String output;
    private String errorMessage;
    private Integer retryCount;
    private Long executedBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private LocalDateTime createdAt;

    public ActionExecution() { this.createdAt = LocalDateTime.now(); }

    public enum ExecutionStatus { PENDING, RUNNING, SUCCESS, FAILED, CANCELLED, TIMEOUT }

    // Getters and Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getActionId() { return actionId; } public void setActionId(Long actionId) { this.actionId = actionId; }
    public String getActionName() { return actionName; } public void setActionName(String actionName) { this.actionName = actionName; }
    public Long getRuleId() { return ruleId; } public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public Long getAlertId() { return alertId; } public void setAlertId(Long alertId) { this.alertId = alertId; }
    public Long getServerId() { return serverId; } public void setServerId(Long serverId) { this.serverId = serverId; }
    public ExecutionStatus getStatus() { return status; } public void setStatus(ExecutionStatus status) { this.status = status; }
    public String getInput() { return input; } public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; } public void setOutput(String output) { this.output = output; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getRetryCount() { return retryCount; } public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Long getExecutedBy() { return executedBy; } public void setExecutedBy(Long executedBy) { this.executedBy = executedBy; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getDuration() { return duration; } public void setDuration(Long duration) { this.duration = duration; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
