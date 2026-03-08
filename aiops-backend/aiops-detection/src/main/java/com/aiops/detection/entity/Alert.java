package com.aiops.detection.entity;

import java.time.Instant;

public record Alert(
        String id,
        String hostname,
        String source,
        String metric,
        String severity,
        double currentValue,
        double threshold,
        String description,
        String status,
        Instant createdAt
) {
}
