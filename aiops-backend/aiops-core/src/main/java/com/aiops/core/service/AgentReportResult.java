package com.aiops.core.service;

import com.aiops.alert.model.Alert;

import java.util.List;
import java.util.Map;

/**
 * Agent report result.
 *
 * <p>Represents the complete MVP pipeline output after a report has been ingested,
 * searched, analyzed and optionally converted into a command dispatch.</p>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public record AgentReportResult(
        String agentId,
        boolean stored,
        List<Alert> alerts,
        String status,
        Map<String, Object> analysis,
        Map<String, Object> command
) {
}
