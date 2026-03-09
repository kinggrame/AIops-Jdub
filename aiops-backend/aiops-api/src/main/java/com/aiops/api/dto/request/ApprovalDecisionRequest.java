package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record ApprovalDecisionRequest(
        @NotBlank(message = "approvalId is required") String approvalId,
        @NotBlank(message = "reviewer is required") String reviewer,
        @NotBlank(message = "decision is required") String decision
) implements Serializable {
}
