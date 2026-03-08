package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record AgentReportRequest(
        @NotBlank(message = "agentId is required") String agentId,
        @NotBlank(message = "hostname is required") String hostname,
        @NotNull(message = "metrics are required") Map<String, Object> metrics,
        List<Map<String, Object>> events
) {
}
