package com.aiops.agent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_messages")
public class AgentMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sessionId;
    private String fromAgent;
    private String toAgent;
    @Enumerated(EnumType.STRING)
    private MessageType type;
    @Column(length = 10000)
    private String content;
    private LocalDateTime timestamp;

    public AgentMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public enum MessageType {
        REQUEST, RESPONSE, TOOL_CALL, TOOL_RESULT, ERROR
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; } public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getFromAgent() { return fromAgent; } public void setFromAgent(String fromAgent) { this.fromAgent = fromAgent; }
    public String getToAgent() { return toAgent; } public void setToAgent(String toAgent) { this.toAgent = toAgent; }
    public MessageType getType() { return type; } public void setType(MessageType type) { this.type = type; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; } public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
