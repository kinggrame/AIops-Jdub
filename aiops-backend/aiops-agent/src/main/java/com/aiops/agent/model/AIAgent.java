package com.aiops.agent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_agents")
public class AIAgent {
    @Id
    private String id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentType type;
    @Column(length = 5000)
    private String systemPrompt;
    @ElementCollection
    private List<String> availableTools;
    private Integer maxIterations = 5;
    private Integer timeout = 300;
    private Boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AIAgent() {
        this.createdAt = LocalDateTime.now();
    }

    public enum AgentType {
        PLANNER, ANALYZER, EXECUTOR, REPORT
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public AgentType getType() { return type; } public void setType(AgentType type) { this.type = type; }
    public String getSystemPrompt() { return systemPrompt; } public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public List<String> getAvailableTools() { return availableTools; } public void setAvailableTools(List<String> availableTools) { this.availableTools = availableTools; }
    public Integer getMaxIterations() { return maxIterations; } public void setMaxIterations(Integer maxIterations) { this.maxIterations = maxIterations; }
    public Integer getTimeout() { return timeout; } public void setTimeout(Integer timeout) { this.timeout = timeout; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
