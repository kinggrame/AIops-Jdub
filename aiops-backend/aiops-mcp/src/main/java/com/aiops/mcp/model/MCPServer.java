package com.aiops.mcp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mcp_servers")
public class MCPServer {
    @Id
    private String id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private MCPType type;
    private String endpoint;
    private String apiKey;
    private String credentials;
    @Enumerated(EnumType.STRING)
    private ServerStatus status = ServerStatus.INACTIVE;
    private Boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MCPServer() {
        this.createdAt = LocalDateTime.now();
    }

    public enum MCPType {
        DINGTALK, WECOM, FEISHU, GITHUB, GITLAB, DATABASE, HTTP, CUSTOM
    }

    public enum ServerStatus {
        ACTIVE, INACTIVE, ERROR
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public MCPType getType() { return type; } public void setType(MCPType type) { this.type = type; }
    public String getEndpoint() { return endpoint; } public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getApiKey() { return apiKey; } public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getCredentials() { return credentials; } public void setCredentials(String credentials) { this.credentials = credentials; }
    public ServerStatus getStatus() { return status; } public void setStatus(ServerStatus status) { this.status = status; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
