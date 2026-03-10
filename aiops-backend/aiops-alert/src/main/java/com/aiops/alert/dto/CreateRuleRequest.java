package com.aiops.alert.dto;

public class CreateRuleRequest {
    private String name;
    private String description;
    private String metricName;
    private String operator;
    private Double threshold;
    private Integer duration;
    private Integer evalInterval;
    private String severity;
    private Long serverId;
    private String serverGroup;
    private String actionConfig;

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

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public String getServerGroup() { return serverGroup; }
    public void setServerGroup(String serverGroup) { this.serverGroup = serverGroup; }

    public String getActionConfig() { return actionConfig; }
    public void setActionConfig(String actionConfig) { this.actionConfig = actionConfig; }
}
