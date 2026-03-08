package com.aiops.core.agent;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultReportAgent implements ReportAgent {

    @Override
    public String buildSummary(Map<String, Object> diagnosis) {
        return "Analysis complete: " + diagnosis.getOrDefault("analysis", "no analysis")
                + " Recommendation: " + diagnosis.getOrDefault("recommendation", "none");
    }
}
