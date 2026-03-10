package com.aiops.api.controller;

import com.aiops.api.dto.request.AgentChatRequest;
import com.aiops.api.dto.request.AgentLogIngestionRequest;
import com.aiops.api.dto.request.AgentRegisterRequest;
import com.aiops.api.dto.request.AgentReportRequest;
import com.aiops.api.dto.request.CommandResultRequest;
import com.aiops.api.dto.response.AgentChatResponse;
import com.aiops.common.model.ApiResponse;
import com.aiops.connection.config.AgentTokenUtils;
import com.aiops.connection.model.CommandResult;
import com.aiops.connection.service.AgentRegistryService;
import com.aiops.connection.service.CommandDispatchService;
import com.aiops.connection.service.PairingTokenService;
import com.aiops.core.service.AgentChatCommand;
import com.aiops.core.service.AgentChatResult;
import com.aiops.core.service.AgentReportCommand;
import com.aiops.core.service.AgentReportFlowService;
import com.aiops.core.service.AgentReportResult;
import com.aiops.core.service.AgentService;
import com.aiops.search.service.SearchService;
import com.aiops.search.ingestion.LogIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentRegistryService agentRegistryService;
    private final SearchService searchService;
    private final AgentService agentService;
    private final CommandDispatchService commandDispatchService;
    private final AgentReportFlowService agentReportFlowService;
    private final LogIngestionService logIngestionService;
    private final PairingTokenService pairingTokenService;

    public AgentController(AgentRegistryService agentRegistryService,
                           SearchService searchService,
                           AgentService agentService,
                           CommandDispatchService commandDispatchService,
                           AgentReportFlowService agentReportFlowService,
                           LogIngestionService logIngestionService,
                           PairingTokenService pairingTokenService) {
        this.agentRegistryService = agentRegistryService;
        this.searchService = searchService;
        this.agentService = agentService;
        this.commandDispatchService = commandDispatchService;
        this.agentReportFlowService = agentReportFlowService;
        this.logIngestionService = logIngestionService;
        this.pairingTokenService = pairingTokenService;
    }

    @PostMapping("/pairing-token")
    public ApiResponse<?> generatePairingToken(@RequestBody Map<String, Object> request) {
        String hostname = (String) request.getOrDefault("hostname", "");
        String ip = (String) request.getOrDefault("ip", "");
        Integer ttlMinutes = (Integer) request.getOrDefault("ttlMinutes", 10);
        
        String token = pairingTokenService.generatePairingToken(
                hostname, 
                ip, 
                Duration.ofMinutes(ttlMinutes != null ? ttlMinutes : 10)
        );
        
        return ApiResponse.ok(Map.of(
                "pairingToken", token,
                "expiresInMinutes", ttlMinutes != null ? ttlMinutes : 10,
                "note", "Use this token within the expiry time to register an agent"
        ), "Pairing token generated");
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
    public ApiResponse<?> report(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @Valid @RequestBody AgentReportRequest request) {
        authenticateAgent(request.agentId(), authorization);
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
    public ApiResponse<?> receiveCommandResult(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               @Valid @RequestBody CommandResultRequest request) {
        authenticateAgent(request.agentId(), authorization);
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
                result.details(),
                result.stages()
        ));
    }

    @PostMapping("/logs")
    public ApiResponse<?> ingestLogs(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @Valid @RequestBody AgentLogIngestionRequest request) {
        authenticateAgent(request.agentId(), authorization);
        logIngestionService.ingest(request.agentId(), request.hostname(), request.logs());
        return ApiResponse.ok(Map.of("stored", true, "count", request.logs().size()), "Logs ingested");
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

    private void authenticateAgent(String agentId, String authorization) {
        String token = AgentTokenUtils.extractBearerToken(authorization);
        if (agentRegistryService.authenticate(agentId, token).isEmpty()) {
            throw new com.aiops.common.exception.BusinessException(HttpStatus.UNAUTHORIZED, "Invalid or missing agent authorization");
        }
    }
}
