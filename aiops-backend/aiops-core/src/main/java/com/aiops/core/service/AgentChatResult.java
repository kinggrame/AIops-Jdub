package com.aiops.core.service;

import java.util.Map;

public record AgentChatResult(
        String conversationId,
        String agentType,
        String provider,
        String reply,
        Map<String, Object> details
) {
}
