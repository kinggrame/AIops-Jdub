package com.aiops.mcp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mcp_tools")
public class MCPTool {
    @Id
    private String id;
    private String serverId;
    private String name;
    private String description;
    @Column(length = 5000)
    private String definition;
    @Column(length = 3000)
    private String inputSchema;
    private Boolean enabled = true;
    private LocalDateTime createdAt;

    public MCPTool() {
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getServerId() { return serverId; } public void setServerId(String serverId) { this.serverId = serverId; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getDefinition() { return definition; } public void setDefinition(String definition) { this.definition = definition; }
    public String getInputSchema() { return inputSchema; } public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
