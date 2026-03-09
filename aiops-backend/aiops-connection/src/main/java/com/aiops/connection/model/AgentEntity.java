package com.aiops.connection.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    @Column(name = "agent_id", length = 36)
    private String agentId;

    @Column(name = "hostname", nullable = false)
    private String hostname;

    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "token", nullable = false)
    private String token;

    @ElementCollection
    @CollectionTable(name = "agent_capabilities", joinColumns = @JoinColumn(name = "agent_id"))
    @Column(name = "capability")
    private java.util.List<String> capabilities;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @Column(name = "latest_metrics", columnDefinition = "TEXT")
    private String latestMetrics;

    public AgentEntity() {}

    public AgentEntity(String agentId, String hostname, String ip, String token,
                       List<String> capabilities, Instant registeredAt, Instant lastSeen) {
        this.agentId = agentId;
        this.hostname = hostname;
        this.ip = ip;
        this.token = token;
        this.capabilities = capabilities;
        this.registeredAt = registeredAt;
        this.lastSeen = lastSeen;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getLatestMetrics() {
        return latestMetrics;
    }

    public void setLatestMetrics(String latestMetrics) {
        this.latestMetrics = latestMetrics;
    }

}
