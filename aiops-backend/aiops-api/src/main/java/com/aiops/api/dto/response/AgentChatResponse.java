package com.aiops.api.dto.response;

import java.util.List;
import java.util.Map;

public record AgentChatResponse(
        String conversationId,
        String agentType,
        String provider,
        String reply,
        Map<String, Object> details,
        List<Map<String, Object>> stages
) {
}
