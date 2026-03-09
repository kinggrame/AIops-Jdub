package com.aiops.command.approval;

import java.time.Instant;
import java.util.Map;

public record ApprovalRequest(
        String approvalId,
        String agentId,
        String command,
        Map<String, Object> params,
        String reason,
        String status,
        String reviewer,
        Instant createdAt,
        Instant reviewedAt
) {

    public ApprovalRequest approve(String reviewer, Instant reviewedAt) {
        return new ApprovalRequest(approvalId, agentId, command, params, reason, "approved", reviewer, createdAt, reviewedAt);
    }

    public ApprovalRequest reject(String reviewer, Instant reviewedAt) {
        return new ApprovalRequest(approvalId, agentId, command, params, reason, "rejected", reviewer, createdAt, reviewedAt);
    }
}
