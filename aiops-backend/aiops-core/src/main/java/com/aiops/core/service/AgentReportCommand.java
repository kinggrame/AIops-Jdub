package com.aiops.core.service;

import java.util.List;
import java.util.Map;

/**
 * Agent report command.
 *
 * <p>Encapsulates the telemetry and trigger payload reported by an agent client for
 * end-to-end processing inside the backend.</p>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public record AgentReportCommand(
        String agentId,
        String hostname,
        Map<String, Object> metrics,
        List<Map<String, Object>> events
) {
}
