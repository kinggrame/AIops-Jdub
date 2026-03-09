package com.aiops.command.approval;

import java.util.List;
import java.util.Map;

public interface ApprovalService {

    ApprovalRequest create(String agentId, String command, Map<String, Object> params, String reason);

    ApprovalRequest approve(String approvalId, String reviewer);

    ApprovalRequest reject(String approvalId, String reviewer);

    List<ApprovalRequest> list();
}
