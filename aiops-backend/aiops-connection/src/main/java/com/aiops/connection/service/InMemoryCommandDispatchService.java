package com.aiops.connection.service;

import com.aiops.common.exception.BusinessException;
import com.aiops.connection.model.CommandEnvelope;
import com.aiops.connection.model.CommandResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ConditionalOnMissingBean(CommandDispatchService.class)
public class InMemoryCommandDispatchService implements CommandDispatchService {

    private final AgentRegistryService agentRegistryService;
    private final AgentSessionService agentSessionService;
    private final List<CommandResult> results = new CopyOnWriteArrayList<>();
    private final Map<String, List<Map<String, Object>>> pendingCommands = new ConcurrentHashMap<>();

    public InMemoryCommandDispatchService(AgentRegistryService agentRegistryService,
                                          AgentSessionService agentSessionService) {
        this.agentRegistryService = agentRegistryService;
        this.agentSessionService = agentSessionService;
    }

    @Override
    public Map<String, Object> dispatch(String agentId, String action, Map<String, Object> params) {
        agentRegistryService.findByAgentId(agentId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Agent not connected: " + agentId));

        Map<String, Object> command = Map.of(
                "commandId", UUID.randomUUID().toString(),
                "agentId", agentId,
                "action", action,
                "params", params,
                "status", agentSessionService.isConnected(agentId) ? "sent" : "queued",
                "createdAt", Instant.now().toString()
        );

        if (agentSessionService.isConnected(agentId)) {
            agentSessionService.send(agentId, new CommandEnvelope(
                    String.valueOf(command.get("commandId")),
                    "execute",
                    action,
                    params
            ));
        } else {
            pendingCommands.computeIfAbsent(agentId, key -> new CopyOnWriteArrayList<>()).add(command);
        }
        return command;
    }

    @Override
    public void recordResult(CommandResult result) {
        results.add(result);
    }

    @Override
    public List<CommandResult> listResults() {
        return new ArrayList<>(results);
    }

    @Override
    public List<Map<String, Object>> listPending(String agentId) {
        return new ArrayList<>(pendingCommands.getOrDefault(agentId, List.of()));
    }
}
