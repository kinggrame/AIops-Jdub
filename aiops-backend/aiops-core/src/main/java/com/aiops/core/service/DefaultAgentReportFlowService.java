package com.aiops.core.service;

import com.aiops.command.service.CommandService;
import com.aiops.alert.model.Alert;
import com.aiops.alert.service.AlertService;
import com.aiops.search.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the agent report flow.
 *
 * <p>This service keeps the report lifecycle in one place so controllers do not need to
 * orchestrate alerting, analysis and command decisions directly.</p>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
@Service
public class DefaultAgentReportFlowService implements AgentReportFlowService {

    private final SearchService searchService;
    private final AlertService alertService;
    private final AgentService agentService;
    private final CommandService commandService;

    public DefaultAgentReportFlowService(SearchService searchService,
                                         AlertService alertService,
                                         AgentService agentService,
                                         CommandService commandService) {
        this.searchService = searchService;
        this.alertService = alertService;
        this.agentService = agentService;
        this.commandService = commandService;
    }

    @Override
    public AgentReportResult process(AgentReportCommand command) {
        List<Map<String, Object>> events = command.events() == null ? List.of() : command.events();
        searchService.indexAgentReport(command.agentId(), command.hostname(), command.metrics(), events);
        List<Alert> alerts = alertService.evaluate(command.hostname(), command.metrics(), events);

        Map<String, Object> analysisMetrics = new LinkedHashMap<>(command.metrics());
        analysisMetrics.put("agentId", command.agentId());

        AgentChatResult analysisResult = agentService.chat(new AgentChatCommand(
                null,
                "analysis",
                buildPrompt(command, alerts),
                analysisMetrics,
                events
        ));

        Map<String, Object> commandDispatch = buildCommandIfNeeded(command, alerts);
        String status = commandDispatch == null ? "analyzed" : "command_dispatched";

        return new AgentReportResult(
                command.agentId(),
                true,
                alerts,
                status,
                analysisResult.details(),
                commandDispatch
        );
    }

    private Map<String, Object> buildCommandIfNeeded(AgentReportCommand command, List<Alert> alerts) {
        boolean hasCritical = alerts.stream().anyMatch(alert -> "critical".equalsIgnoreCase(alert.severity()));
        if (!hasCritical) {
            return null;
        }
        String action = inferAction(command.metrics());
        return commandService.dispatchToAgent(command.agentId(), action, defaultParams(action));
    }

    private String inferAction(Map<String, Object> metrics) {
        double cpu = nested(metrics, "cpu", "usage");
        double memory = nested(metrics, "memory", "usage");
        if (cpu >= 90) {
            return "get_logs";
        }
        if (memory >= 85) {
            return "clear_cache";
        }
        return "get_logs";
    }

    private Map<String, Object> defaultParams(String action) {
        return switch (action) {
            case "clear_cache" -> Map.of("scope", "application");
            case "restart_service" -> Map.of("service", "nginx");
            default -> Map.of("lines", 200);
        };
    }

    private String buildPrompt(AgentReportCommand command, List<Alert> alerts) {
        return "Analyze report for host=" + command.hostname() + ", alerts=" + alerts.size() + ", metrics=" + command.metrics();
    }

    private double nested(Map<String, Object> metrics, String group, String key) {
        Object value = metrics.get(group);
        if (value instanceof Map<?, ?> map && map.get(key) instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }
}
