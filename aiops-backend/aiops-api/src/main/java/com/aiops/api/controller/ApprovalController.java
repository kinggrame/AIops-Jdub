package com.aiops.api.controller;

import com.aiops.api.dto.request.ApprovalDecisionRequest;
import com.aiops.command.approval.ApprovalRequest;
import com.aiops.command.approval.ApprovalService;
import com.aiops.command.service.CommandService;
import com.aiops.common.model.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;
    private final CommandService commandService;

    public ApprovalController(ApprovalService approvalService, CommandService commandService) {
        this.approvalService = approvalService;
        this.commandService = commandService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(approvalService.list());
    }

    @PostMapping("/decision")
    public ApiResponse<?> decide(@Valid @RequestBody ApprovalDecisionRequest request) {
        if ("approve".equalsIgnoreCase(request.decision())) {
            ApprovalRequest approved = approvalService.approve(request.approvalId(), request.reviewer());
            Map<String, Object> dispatch = commandService.dispatchToAgent(approved.agentId(), approved.command(), approved.params());
            return ApiResponse.ok(Map.of("approval", approved, "dispatch", dispatch), "Approval executed");
        }

        ApprovalRequest rejected = approvalService.reject(request.approvalId(), request.reviewer());
        return ApiResponse.ok(rejected, "Approval rejected");
    }
}
