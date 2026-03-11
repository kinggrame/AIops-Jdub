package com.aiops.llm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "llm_providers")
public class LlmProvider {
    @Id
    private String id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ProviderType type;
    private String apiKey;
    private String endpoint;
    @Enumerated(EnumType.STRING)
    private ModelType modelType;
    private String defaultModel;
    private Double temperature = 0.7;
    private Integer maxTokens = 2000;
    private Integer dimensions = 1536;
    private Integer priority = 1;
    private Boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LlmProvider() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ProviderType {
        OPENAI, ANTHROPIC, OLLAMA, CUSTOM
    }

    public enum ModelType {
        CHAT, EMBEDDING
    }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public ProviderType getType() { return type; } public void setType(ProviderType type) { this.type = type; }
    public String getApiKey() { return apiKey; } public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getEndpoint() { return endpoint; } public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public ModelType getModelType() { return modelType; } public void setModelType(ModelType modelType) { this.modelType = modelType; }
    public String getDefaultModel() { return defaultModel; } public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }
    public Double getTemperature() { return temperature; } public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxTokens() { return maxTokens; } public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Integer getDimensions() { return dimensions; } public void setDimensions(Integer dimensions) { this.dimensions = dimensions; }
    public Integer getPriority() { return priority; } public void setPriority(Integer priority) { this.priority = priority; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
