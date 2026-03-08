package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record AgentChatRequest(
        String conversationId,
        @NotBlank(message = "agentType is required") String agentType,
        @NotBlank(message = "message is required") String message,
        Map<String, Object> metrics,
        List<Map<String, Object>> events
) {
}
