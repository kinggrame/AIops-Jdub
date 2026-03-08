package com.aiops.connection.model;

import java.time.Instant;

public record CommandResult(
        String commandId,
        String agentId,
        String status,
        String output,
        Instant timestamp
) {
}
