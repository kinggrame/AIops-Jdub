package com.aiops.connection.service;

import com.aiops.common.exception.BusinessException;
import com.aiops.connection.model.CommandEnvelope;
import com.aiops.connection.model.CommandResult;
import com.aiops.connection.model.CommandResultEntity;
import com.aiops.connection.repository.CommandResultRepository;
import org.springframework.context.annotation.Primary;
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
@Primary
public class JpaCommandDispatchService implements CommandDispatchService {

    private final AgentRegistryService agentRegistryService;
    private final AgentSessionService agentSessionService;
    private final CommandResultRepository commandResultRepository;
    private final Map<String, List<Map<String, Object>>> pendingCommands = new ConcurrentHashMap<>();

    public JpaCommandDispatchService(AgentRegistryService agentRegistryService,
                                     AgentSessionService agentSessionService,
                                     CommandResultRepository commandResultRepository) {
        this.agentRegistryService = agentRegistryService;
        this.agentSessionService = agentSessionService;
        this.commandResultRepository = commandResultRepository;
    }

    @Override
    public Map<String, Object> dispatch(String agentId, String action, Map<String, Object> params) {
        if (agentRegistryService.findByAgentId(agentId).isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Agent not connected: " + agentId);
        }

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
        CommandResultEntity entity = new CommandResultEntity();
        entity.setCommandId(result.commandId());
        entity.setAgentId(result.agentId());
        entity.setStatus(result.status());
        entity.setOutput(result.output());
        entity.setTimestamp(result.timestamp());
        commandResultRepository.save(entity);
    }

    @Override
    public List<CommandResult> listResults() {
        return commandResultRepository.findAll().stream()
                .map(entity -> new CommandResult(
                        entity.getCommandId(),
                        entity.getAgentId(),
                        entity.getStatus(),
                        entity.getOutput(),
                        entity.getTimestamp()
                ))
                .toList();
    }

    @Override
    public List<Map<String, Object>> listPending(String agentId) {
        return new ArrayList<>(pendingCommands.getOrDefault(agentId, List.of()));
    }
}
