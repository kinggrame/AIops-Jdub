package com.aiops.api.controller;

import com.aiops.api.dto.request.AgentChatRequest;
import com.aiops.api.dto.request.AgentRegisterRequest;
import com.aiops.api.dto.request.AgentReportRequest;
import com.aiops.api.dto.request.CommandResultRequest;
import com.aiops.api.dto.response.AgentChatResponse;
import com.aiops.common.model.ApiResponse;
import com.aiops.connection.model.CommandResult;
import com.aiops.connection.service.AgentRegistryService;
import com.aiops.connection.service.CommandDispatchService;
import com.aiops.core.service.AgentChatCommand;
import com.aiops.core.service.AgentChatResult;
import com.aiops.core.service.AgentReportCommand;
import com.aiops.core.service.AgentReportFlowService;
import com.aiops.core.service.AgentReportResult;
import com.aiops.core.service.AgentService;
import com.aiops.detection.entity.Alert;
import com.aiops.detection.service.AlertService;
import com.aiops.search.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentRegistryService agentRegistryService;
    private final SearchService searchService;
    private final AlertService alertService;
    private final AgentService agentService;
    private final CommandDispatchService commandDispatchService;
    private final AgentReportFlowService agentReportFlowService;

    public AgentController(AgentRegistryService agentRegistryService,
                           SearchService searchService,
                           AlertService alertService,
                           AgentService agentService,
                           CommandDispatchService commandDispatchService,
                           AgentReportFlowService agentReportFlowService) {
        this.agentRegistryService = agentRegistryService;
        this.searchService = searchService;
        this.alertService = alertService;
        this.agentService = agentService;
        this.commandDispatchService = commandDispatchService;
        this.agentReportFlowService = agentReportFlowService;
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody AgentRegisterRequest request) {
        return ApiResponse.ok(agentRegistryService.register(
                request.hostname(),
                request.ip(),
                request.token(),
                request.capabilities()
        ), "Agent registered");
    }

    @PostMapping("/report")
    public ApiResponse<?> report(@Valid @RequestBody AgentReportRequest request) {
        Map<String, Object> metrics = request.metrics();
        List<Map<String, Object>> events = request.events() == null ? List.of() : request.events();
        agentRegistryService.heartbeat(request.agentId(), metrics);
        AgentReportResult result = agentReportFlowService.process(new AgentReportCommand(
                request.agentId(),
                request.hostname(),
                metrics,
                events
        ));
        return ApiResponse.ok(result, "Report processed");
    }

    @PostMapping("/command/result")
    public ApiResponse<?> receiveCommandResult(@Valid @RequestBody CommandResultRequest request) {
        commandDispatchService.recordResult(new CommandResult(
                request.commandId(),
                request.agentId(),
                request.status(),
                request.output(),
                Instant.now()
        ));
        return ApiResponse.ok(Map.of("accepted", true), "Command result recorded");
    }

    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        AgentChatResult result = agentService.chat(new AgentChatCommand(
                request.conversationId(),
                request.agentType(),
                request.message(),
                request.metrics() == null ? Map.of() : request.metrics(),
                request.events() == null ? List.of() : request.events()
        ));
        return ApiResponse.ok(new AgentChatResponse(
                result.conversationId(),
                result.agentType(),
                result.provider(),
                result.reply(),
                result.details()
        ));
    }

    @GetMapping("/clients")
    public ApiResponse<?> listAgents() {
        return ApiResponse.ok(agentRegistryService.listAgents());
    }

    @GetMapping("/command/results")
    public ApiResponse<?> listCommandResults() {
        return ApiResponse.ok(commandDispatchService.listResults());
    }

    @GetMapping("/command/pending")
    public ApiResponse<?> listPendingCommands() {
        return ApiResponse.ok(agentRegistryService.listAgents().stream()
                .collect(java.util.stream.Collectors.toMap(
                        agent -> agent.agentId(),
                        agent -> commandDispatchService.listPending(agent.agentId())
                )));
    }
}
