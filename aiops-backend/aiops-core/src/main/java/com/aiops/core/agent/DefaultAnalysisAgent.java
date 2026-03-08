package com.aiops.core.agent;

import com.aiops.command.service.CommandService;
import com.aiops.detection.entity.Alert;
import com.aiops.detection.service.AlertService;
import com.aiops.rag.entity.KnowledgeDocument;
import com.aiops.rag.service.KnowledgeService;
import com.aiops.search.service.SearchService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultAnalysisAgent implements AnalysisAgent {

    private final KnowledgeService knowledgeService;
    private final SearchService searchService;
    private final AlertService alertService;
    private final CommandService commandService;

    public DefaultAnalysisAgent(KnowledgeService knowledgeService,
                                SearchService searchService,
                                AlertService alertService,
                                CommandService commandService) {
        this.knowledgeService = knowledgeService;
        this.searchService = searchService;
        this.alertService = alertService;
        this.commandService = commandService;
    }

    @Override
    public Map<String, Object> diagnose(String userMessage, Map<String, Object> metrics, List<Map<String, Object>> events) {
        List<KnowledgeDocument> knowledge = knowledgeService.search(userMessage, 3);
        List<Alert> alerts = alertService.evaluate("manual-chat", metrics, events);
        String recommendation = recommend(metrics, knowledge);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("analysis", buildAnalysis(metrics, alerts));
        result.put("knowledge", knowledge);
        result.put("relatedLogs", searchService.searchLogs(userMessage, 5));
        result.put("recommendation", recommendation);
        if (!events.isEmpty() && metrics.containsKey("agentId")) {
            result.put("action", commandService.dispatchToAgent(String.valueOf(metrics.get("agentId")), "get_logs", Map.of("lines", 200)));
        }
        return result;
    }

    private String buildAnalysis(Map<String, Object> metrics, List<Alert> alerts) {
        double cpu = nested(metrics, "cpu", "usage");
        double memory = nested(metrics, "memory", "usage");
        if (!alerts.isEmpty()) {
            return "Detected " + alerts.size() + " active alerts; CPU=" + cpu + "% memory=" + memory + "%";
        }
        return "Current telemetry looks stable; CPU=" + cpu + "% memory=" + memory + "%";
    }

    private String recommend(Map<String, Object> metrics, List<KnowledgeDocument> knowledge) {
        double cpu = nested(metrics, "cpu", "usage");
        if (cpu >= 90) {
            return "Inspect high-CPU process and consider restarting the impacted service.";
        }
        if (!knowledge.isEmpty()) {
            return knowledge.get(0).content();
        }
        return "Collect more logs before taking action.";
    }

    private double nested(Map<String, Object> metrics, String group, String key) {
        Object value = metrics.get(group);
        if (value instanceof Map<?, ?> map && map.get(key) instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }
}
