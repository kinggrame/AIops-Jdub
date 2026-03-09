package com.aiops.command.approval;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_requests")
public class ApprovalEntity {

    @Id
    @Column(name = "approval_id", length = 36)
    private String approvalId;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "command", nullable = false)
    private String command;

    @Column(name = "params", columnDefinition = "TEXT")
    private String params;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "reviewer")
    private String reviewer;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public ApprovalEntity() {}

    public ApprovalEntity(String approvalId, String agentId, String command, 
                          String params, String reason, String status, 
                          String reviewer, Instant createdAt, Instant reviewedAt) {
        this.approvalId = approvalId;
        this.agentId = agentId;
        this.command = command;
        this.params = params;
        this.reason = reason;
        this.status = status;
        this.reviewer = reviewer;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public String getApprovalId() { return approvalId; }
    public void setApprovalId(String approvalId) { this.approvalId = approvalId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
