package com.aiops.core.agent;

import com.aiops.rag.service.KnowledgeService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultPlanAgent implements PlanAgent {

    private final KnowledgeService knowledgeService;

    public DefaultPlanAgent(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Override
    public Map<String, Object> plan(String message, Map<String, Object> summary) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("goal", "Investigate incident and collect next-hop evidence");
        plan.put("steps", knowledgeService.search(message, 2).stream().map(doc -> doc.title()).toList());
        plan.put("risk", "low");
        plan.put("recommendedCommand", cpu(summary) >= 90 ? "top" : "ps aux");
        plan.put("reason", "Need live process evidence before remediation");
        return plan;
    }

    private double cpu(Map<String, Object> summary) {
        Object metrics = summary.get("metrics");
        if (metrics instanceof Map<?, ?> metricMap) {
            Object cpu = metricMap.get("cpu");
            if (cpu instanceof Map<?, ?> cpuMap && cpuMap.get("usage") instanceof Number number) {
                return number.doubleValue();
            }
        }
        return 0;
    }
}
