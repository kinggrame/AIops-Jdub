package com.aiops.core.agent;

import com.aiops.command.approval.ApprovalRequest;
import com.aiops.command.approval.ApprovalService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultExecuteAgent implements ExecuteAgent {

    private final ApprovalService approvalService;

    public DefaultExecuteAgent(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Override
    public Map<String, Object> propose(String agentId, String message, Map<String, Object> plan) {
        String command = String.valueOf(plan.getOrDefault("recommendedCommand", "top"));
        ApprovalRequest approvalRequest = approvalService.create(
                agentId,
                command,
                Map.of("source", "execute-agent"),
                String.valueOf(plan.getOrDefault("reason", message))
        );
        Map<String, Object> proposal = new LinkedHashMap<>();
        proposal.put("command", command);
        proposal.put("requiresApproval", true);
        proposal.put("approval", approvalRequest);
        return proposal;
    }
}
