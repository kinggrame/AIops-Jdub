package com.aiops.core.entity;

import java.time.Instant;
import java.util.List;

public record Conversation(
        String conversationId,
        String agentType,
        List<Message> messages,
        Instant createdAt
) {
}
