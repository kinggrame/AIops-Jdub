package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record AgentLogIngestionRequest(
        @NotBlank(message = "agentId is required") String agentId,
        @NotBlank(message = "hostname is required") String hostname,
        @NotEmpty(message = "logs are required") List<Map<String, Object>> logs
) implements Serializable {
}
