package com.aiops.agent.memory;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agent_memories")
public class AgentMemory {
    @Id
    private String id;
    private String sessionId;
    private String userId;
    @Column(columnDefinition = "TEXT")
    private String compressedHistory;
    private Integer messageCount;
    private Integer compressionLevel;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    public AgentMemory() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.messageCount = 0;
        this.compressionLevel = 1;
    }

    public static class Message {
        public String role;
        public String content;
        public String node;
        public long timestamp;

        public Message() {}

        public Message(String role, String content, String node) {
            this.role = role;
            this.content = content;
            this.node = node;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCompressedHistory() { return compressedHistory; }
    public void setCompressedHistory(String compressedHistory) { this.compressedHistory = compressedHistory; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    public Integer getCompressionLevel() { return compressionLevel; }
    public void setCompressionLevel(Integer compressionLevel) { this.compressionLevel = compressionLevel; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
