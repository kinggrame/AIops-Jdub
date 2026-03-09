package com.aiops.connection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "command_results")
public class CommandResultEntity {

    @Id
    @Column(name = "command_id", length = 36)
    private String commandId;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "output", columnDefinition = "TEXT")
    private String output;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
