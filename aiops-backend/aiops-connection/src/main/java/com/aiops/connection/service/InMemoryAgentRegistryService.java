package com.aiops.connection.service;

import com.aiops.common.exception.BusinessException;
import com.aiops.connection.config.AgentTokenUtils;
import com.aiops.connection.config.AgentTokenProperties;
import com.aiops.connection.model.AgentInfo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryAgentRegistryService implements AgentRegistryService {

    private final AgentTokenProperties tokenProperties;
    private final Map<String, AgentInfo> agents = new ConcurrentHashMap<>();

    public InMemoryAgentRegistryService(AgentTokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    @Override
    public AgentInfo register(String hostname, String ip, String token, List<String> capabilities) {
        String agentId = UUID.nameUUIDFromBytes((hostname + ip).getBytes()).toString();
        Instant now = Instant.now();
        AgentInfo existing = agents.get(agentId);
        if (existing != null) {
            if (!matchesStoredToken(existing, token) && !isBootstrapToken(token)) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid agent token");
            }
            String issuedToken = existing.token() == null || existing.token().isBlank()
                    ? AgentTokenUtils.issueAgentToken(tokenProperties.getSeed(), hostname, ip)
                    : existing.token();
            AgentInfo refreshed = new AgentInfo(agentId, hostname, ip, issuedToken, capabilities, existing.registeredAt(), now, existing.latestMetrics());
            agents.put(agentId, refreshed);
            return refreshed;
        }
        validateBootstrapToken(token);
        AgentInfo agentInfo = new AgentInfo(
                agentId,
                hostname,
                ip,
                AgentTokenUtils.issueAgentToken(tokenProperties.getSeed(), hostname, ip),
                capabilities,
                now,
                now,
                Map.of()
        );
        agents.put(agentId, agentInfo);
        return agentInfo;
    }

    @Override
    public Optional<AgentInfo> findByAgentId(String agentId) {
        return Optional.ofNullable(agents.get(agentId));
    }

    @Override
    public Optional<AgentInfo> findByHostname(String hostname) {
        return agents.values().stream().filter(agent -> agent.hostname().equals(hostname)).findFirst();
    }

    @Override
    public Optional<AgentInfo> authenticate(String agentId, String token) {
        AgentInfo agent = agents.get(agentId);
        if (agent == null || token == null || token.isBlank()) {
            return Optional.empty();
        }
        return token.equals(agent.token()) ? Optional.of(agent) : Optional.empty();
    }

    @Override
    public List<AgentInfo> listAgents() {
        return agents.values().stream().toList();
    }

    @Override
    public void heartbeat(String agentId, Map<String, Object> metrics) {
        AgentInfo existing = agents.get(agentId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Agent not found: " + agentId);
        }
        agents.put(agentId, existing.withHeartbeat(Instant.now(), metrics));
    }

    private void validateBootstrapToken(String token) {
        if (!isBootstrapToken(token)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid agent token");
        }
    }

    private boolean isBootstrapToken(String token) {
        return token != null && token.startsWith(tokenProperties.getSeed());
    }

    private boolean matchesStoredToken(AgentInfo agent, String token) {
        return token != null && token.equals(agent.token());
    }
}
