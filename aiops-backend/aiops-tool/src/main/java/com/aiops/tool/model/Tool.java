package com.aiops.tool.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tools")
public class Tool {
    @Id private String id;
    @Column(nullable = false) private String name;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ToolType type;
    @Enumerated(EnumType.STRING) private ToolCategory category;
    @Column(columnDefinition = "TEXT") private String definition;
    @Column(columnDefinition = "TEXT") private String inputSchema;
    private Boolean enabled = true;
    private Integer timeout = 30;
    private Integer cacheTtl = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Tool() { this.createdAt = LocalDateTime.now(); }
    
    public enum ToolType { SCRIPT, ELK, MILVUS, AGENT, NOTIFY, MCP, HTTP }
    
    public enum ToolCategory { 
        MONITORING("监控"), 
        LOGGING("日志"), 
        NOTIFICATION("通知"), 
        EXECUTION("执行"), 
        KNOWLEDGE("知识库"), 
        ANALYTICS("分析"),
        OTHER("其他");

        private final String label;
        ToolCategory(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public ToolType getType() { return type; } public void setType(ToolType type) { this.type = type; }
    public ToolCategory getCategory() { return category; } public void setCategory(ToolCategory category) { this.category = category; }
    public String getDefinition() { return definition; } public void setDefinition(String definition) { this.definition = definition; }
    public String getInputSchema() { return inputSchema; } public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getTimeout() { return timeout; } public void setTimeout(Integer timeout) { this.timeout = timeout; }
    public Integer getCacheTtl() { return cacheTtl; } public void setCacheTtl(Integer cacheTtl) { this.cacheTtl = cacheTtl; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
