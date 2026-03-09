package com.aiops.command.approval;

import com.aiops.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryApprovalService implements ApprovalService {

    private final Map<String, ApprovalRequest> approvals = new ConcurrentHashMap<>();

    @Override
    public ApprovalRequest create(String agentId, String command, Map<String, Object> params, String reason) {
        ApprovalRequest request = new ApprovalRequest(
                UUID.randomUUID().toString(),
                agentId,
                command,
                params,
                reason,
                "pending",
                null,
                Instant.now(),
                null
        );
        approvals.put(request.approvalId(), request);
        return request;
    }

    @Override
    public ApprovalRequest approve(String approvalId, String reviewer) {
        ApprovalRequest request = getOrThrow(approvalId);
        ApprovalRequest approved = request.approve(reviewer, Instant.now());
        approvals.put(approvalId, approved);
        return approved;
    }

    @Override
    public ApprovalRequest reject(String approvalId, String reviewer) {
        ApprovalRequest request = getOrThrow(approvalId);
        ApprovalRequest rejected = request.reject(reviewer, Instant.now());
        approvals.put(approvalId, rejected);
        return rejected;
    }

    @Override
    public List<ApprovalRequest> list() {
        return new ArrayList<>(approvals.values());
    }

    private ApprovalRequest getOrThrow(String approvalId) {
        ApprovalRequest request = approvals.get(approvalId);
        if (request == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Approval request not found: " + approvalId);
        }
        return request;
    }
}
