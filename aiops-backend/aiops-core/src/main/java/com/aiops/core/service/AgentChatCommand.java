package com.aiops.core.service;

import java.util.List;
import java.util.Map;

public record AgentChatCommand(
        String conversationId,
        String agentType,
        String message,
        Map<String, Object> metrics,
        List<Map<String, Object>> events
) {
}
