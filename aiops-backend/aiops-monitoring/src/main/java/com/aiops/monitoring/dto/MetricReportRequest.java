package com.aiops.monitoring.dto;

import java.util.Map;

public class MetricReportRequest {

    private Long serverId;
    private String agentId;
    private Map<String, Object> metrics;
    private String timestamp;

    public MetricReportRequest() {}

    // Getters and Setters

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
