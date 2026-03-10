package com.aiops.action.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actions")
public class Action {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ActionType type;
    private Long ruleId;
    @Enumerated(EnumType.STRING) private ActionTriggerType triggerType;
    @Column(columnDefinition = "TEXT") private String config;
    @Enumerated(EnumType.STRING) private ActionStatus status = ActionStatus.ENABLED;
    private Integer order;
    private Integer timeout = 30;
    private Integer retryCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Action() { this.createdAt = LocalDateTime.now(); }

    public enum ActionType { EXECUTE_SCRIPT, EXECUTE_SKILL, SEND_NOTIFICATION, APPROVAL, WEBHOOK }
    public enum ActionTriggerType { ON_TRIGGER, ON_RESOLVE }
    public enum ActionStatus { ENABLED, DISABLED }

    // Getters and Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public ActionType getType() { return type; } public void setType(ActionType type) { this.type = type; }
    public Long getRuleId() { return ruleId; } public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public ActionTriggerType getTriggerType() { return triggerType; } public void setTriggerType(ActionTriggerType triggerType) { this.triggerType = triggerType; }
    public String getConfig() { return config; } public void setConfig(String config) { this.config = config; }
    public ActionStatus getStatus() { return status; } public void setStatus(ActionStatus status) { this.status = status; }
    public Integer getOrder() { return order; } public void setOrder(Integer order) { this.order = order; }
    public Integer getTimeout() { return timeout; } public void setTimeout(Integer timeout) { this.timeout = timeout; }
    public Integer getRetryCount() { return retryCount; } public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
