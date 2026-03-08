package com.aiops.search.entity;

import java.time.Instant;
import java.util.Map;

public record LogEntry(
        String id,
        String agentId,
        String hostname,
        String level,
        String message,
        Map<String, Object> metadata,
        Instant timestamp
) {
}
