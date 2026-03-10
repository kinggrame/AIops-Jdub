package com.aiops.agent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "agent_sessions")
public class AgentSession {
    @Id
    private String id;
    private String userId;
    private String status;
    @Column(length = 10000)
    private String initialRequest;
    @Column(length = 10000)
    private String finalReport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AgentSession() {
        this.createdAt = LocalDateTime.now();
        this.status = "RUNNING";
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getInitialRequest() { return initialRequest; } public void setInitialRequest(String initialRequest) { this.initialRequest = initialRequest; }
    public String getFinalReport() { return finalReport; } public void setFinalReport(String finalReport) { this.finalReport = finalReport; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
