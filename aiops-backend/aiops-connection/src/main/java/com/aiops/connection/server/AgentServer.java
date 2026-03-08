package com.aiops.connection.server;

import com.aiops.connection.service.AgentRegistryService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentServer {

    private final AgentRegistryService agentRegistryService;

    public AgentServer(AgentRegistryService agentRegistryService) {
        this.agentRegistryService = agentRegistryService;
    }

    public void receiveHeartbeat(String agentId, Map<String, Object> metrics) {
        agentRegistryService.heartbeat(agentId, metrics);
    }
}
