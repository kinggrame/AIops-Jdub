package com.aiops.config.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs")
public class SystemConfig {
    @Id
    private String key;
    private String value;
    private String description;
    @Enumerated(EnumType.STRING)
    private ConfigType type = ConfigType.STRING;
    private String category;
    private Boolean editable = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SystemConfig() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ConfigType {
        STRING, NUMBER, BOOLEAN, JSON
    }

    public String getKey() { return key; } public void setKey(String key) { this.key = key; }
    public String getValue() { return value; } public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public ConfigType getType() { return type; } public void setType(ConfigType type) { this.type = type; }
    public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
    public Boolean getEditable() { return editable; } public void setEditable(Boolean editable) { this.editable = editable; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
