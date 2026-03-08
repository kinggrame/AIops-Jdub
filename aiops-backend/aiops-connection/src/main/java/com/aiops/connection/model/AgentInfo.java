package com.aiops.connection.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AgentInfo(
        String agentId,
        String hostname,
        String ip,
        String token,
        List<String> capabilities,
        Instant registeredAt,
        Instant lastSeen,
        Map<String, Object> latestMetrics
) {

    public AgentInfo withHeartbeat(Instant time, Map<String, Object> metrics) {
        return new AgentInfo(agentId, hostname, ip, token, capabilities, registeredAt, time, metrics);
    }
}
