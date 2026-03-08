package com.aiops.connection.model;

import java.util.Map;

public record CommandEnvelope(
        String id,
        String cmd,
        String action,
        Map<String, Object> params
) {
}
