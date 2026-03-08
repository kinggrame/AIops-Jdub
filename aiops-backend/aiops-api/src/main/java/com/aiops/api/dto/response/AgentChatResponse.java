package com.aiops.api.dto.response;

import java.util.Map;

public record AgentChatResponse(
        String conversationId,
        String agentType,
        String provider,
        String reply,
        Map<String, Object> details
) {
}
