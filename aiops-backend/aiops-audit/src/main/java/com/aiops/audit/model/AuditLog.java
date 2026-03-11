package com.aiops.audit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username = "system";
    private String action;
    private String resource;
    private String resourceId;
    private String details;
    private String ipAddress;
    @Enumerated(EnumType.STRING)
    private AuditResult result = AuditResult.SUCCESS;
    private LocalDateTime createdAt;

    public AuditLog() {
        this.createdAt = LocalDateTime.now();
    }

    public enum AuditResult {
        SUCCESS, FAILURE
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public String getResource() { return resource; } public void setResource(String resource) { this.resource = resource; }
    public String getResourceId() { return resourceId; } public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getDetails() { return details; } public void setDetails(String details) { this.details = details; }
    public String getIpAddress() { return ipAddress; } public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public AuditResult getResult() { return result; } public void setResult(AuditResult result) { this.result = result; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
