package com.aiops.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_channels")
public class NotificationChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ChannelType type;
    private String config;
    @Enumerated(EnumType.STRING)
    private ChannelStatus status = ChannelStatus.INACTIVE;
    private Boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public NotificationChannel() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ChannelType {
        DINGTALK, WECOM, FEISHU, EMAIL, SMS, WEBHOOK
    }

    public enum ChannelStatus {
        ACTIVE, INACTIVE, ERROR
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public ChannelType getType() { return type; } public void setType(ChannelType type) { this.type = type; }
    public String getConfig() { return config; } public void setConfig(String config) { this.config = config; }
    public ChannelStatus getStatus() { return status; } public void setStatus(ChannelStatus status) { this.status = status; }
    public Boolean getEnabled() { return enabled; } public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
