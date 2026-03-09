package com.aiops.core.orchestrator;

import com.aiops.core.agent.ExecuteAgent;
import com.aiops.core.agent.PlanAgent;
import com.aiops.core.agent.ReportAgent;
import com.aiops.core.agent.SummarizeAgent;
import com.aiops.search.entity.LogEntry;
import com.aiops.search.service.SearchService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MultiAgentOrchestrator {

    private final SummarizeAgent summarizeAgent;
    private final PlanAgent planAgent;
    private final ExecuteAgent executeAgent;
    private final ReportAgent reportAgent;
    private final SearchService searchService;

    public MultiAgentOrchestrator(SummarizeAgent summarizeAgent,
                                  PlanAgent planAgent,
                                  ExecuteAgent executeAgent,
                                  ReportAgent reportAgent,
                                  SearchService searchService) {
        this.summarizeAgent = summarizeAgent;
        this.planAgent = planAgent;
        this.executeAgent = executeAgent;
        this.reportAgent = reportAgent;
        this.searchService = searchService;
    }

    public Map<String, Object> run(String agentId, String message, Map<String, Object> metrics, List<Map<String, Object>> events) {
        List<Map<String, Object>> logs = searchService.searchLogs(message, 5).stream()
                .map(this::toMap)
                .toList();

        Map<String, Object> summary = summarizeAgent.summarize(message, metrics, events, logs);
        Map<String, Object> plan = planAgent.plan(message, summary);
        Map<String, Object> execute = executeAgent.propose(agentId, message, plan);

        Map<String, Object> reportInput = new LinkedHashMap<>();
        reportInput.put("summary", summary);
        reportInput.put("plan", plan);
        reportInput.put("execute", execute);

        String report = reportAgent.buildSummary(reportInput);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("summaryStage", summary);
        details.put("planStage", plan);
        details.put("executeStage", execute);
        details.put("reportStage", report);
        details.put("stages", List.of(
                Map.of("agent", "SUMMARIZE", "output", summary.get("summary")),
                Map.of("agent", "PLAN", "output", plan.get("reason")),
                Map.of("agent", "EXECUTE", "output", execute.get("command")),
                Map.of("agent", "REPORT", "output", report)
        ));
        return details;
    }

    private Map<String, Object> toMap(LogEntry logEntry) {
        return Map.of(
                "hostname", logEntry.hostname(),
                "level", logEntry.level(),
                "message", logEntry.message(),
                "timestamp", logEntry.timestamp().toString()
        );
    }
}
