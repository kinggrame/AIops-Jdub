package com.aiops.connection.service;

import com.aiops.common.exception.BusinessException;
import com.aiops.connection.config.AgentTokenUtils;
import com.aiops.connection.config.AgentTokenProperties;
import com.aiops.connection.model.AgentEntity;
import com.aiops.connection.model.AgentInfo;
import com.aiops.connection.repository.AgentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class JpaAgentRegistryService implements AgentRegistryService {

    private final AgentTokenProperties tokenProperties;
    private final AgentRepository agentRepository;
    private final ObjectMapper objectMapper;

    public JpaAgentRegistryService(AgentTokenProperties tokenProperties, 
                                   AgentRepository agentRepository,
                                   ObjectMapper objectMapper) {
        this.tokenProperties = tokenProperties;
        this.agentRepository = agentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AgentInfo register(String hostname, String ip, String token, List<String> capabilities) {
        String agentId = UUID.nameUUIDFromBytes((hostname + ip).getBytes()).toString();
        Instant now = Instant.now();

        AgentEntity entity = agentRepository.findById(agentId).orElse(null);
        if (entity != null) {
            if (!matchesStoredToken(entity, token) && !isBootstrapToken(token)) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid agent token");
            }
            if (entity.getToken() == null || entity.getToken().isBlank()) {
                entity.setToken(AgentTokenUtils.issueAgentToken(tokenProperties.getSeed(), hostname, ip));
            }
            entity.setHostname(hostname);
            entity.setIp(ip);
            entity.setCapabilities(capabilities);
            entity.setLastSeen(now);
        } else {
            validateBootstrapToken(token);
            entity = new AgentEntity(
                    agentId,
                    hostname,
                    ip,
                    AgentTokenUtils.issueAgentToken(tokenProperties.getSeed(), hostname, ip),
                    capabilities,
                    now,
                    now
            );
        }
        agentRepository.save(entity);

        return toAgentInfo(entity);
    }

    @Override
    public Optional<AgentInfo> findByAgentId(String agentId) {
        return agentRepository.findById(agentId).map(this::toAgentInfo);
    }

    @Override
    public Optional<AgentInfo> findByHostname(String hostname) {
        return agentRepository.findByHostname(hostname).map(this::toAgentInfo);
    }

    @Override
    public Optional<AgentInfo> authenticate(String agentId, String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return agentRepository.findById(agentId)
                .filter(entity -> token.equals(entity.getToken()))
                .map(this::toAgentInfo);
    }

    @Override
    public List<AgentInfo> listAgents() {
        return agentRepository.findAll().stream()
                .map(this::toAgentInfo)
                .collect(Collectors.toList());
    }

    @Override
    public void heartbeat(String agentId, Map<String, Object> metrics) {
        AgentEntity existing = agentRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Agent not found: " + agentId));
        
        existing.setLastSeen(Instant.now());
        try {
            existing.setLatestMetrics(objectMapper.writeValueAsString(metrics));
        } catch (JsonProcessingException e) {
            // ignore
        }
        agentRepository.save(existing);
    }

    private void validateBootstrapToken(String token) {
        if (!isBootstrapToken(token)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid agent token");
        }
    }

    private boolean isBootstrapToken(String token) {
        return token != null && token.startsWith(tokenProperties.getSeed());
    }

    private boolean matchesStoredToken(AgentEntity entity, String token) {
        return token != null && token.equals(entity.getToken());
    }

    private AgentInfo toAgentInfo(AgentEntity entity) {
        Map<String, Object> metrics = null;
        if (entity.getLatestMetrics() != null) {
            try {
                metrics = objectMapper.readValue(entity.getLatestMetrics(), 
                    new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                // ignore
            }
        }
        return new AgentInfo(
                entity.getAgentId(),
                entity.getHostname(),
                entity.getIp(),
                entity.getToken(),
                entity.getCapabilities(),
                entity.getRegisteredAt(),
                entity.getLastSeen(),
                metrics != null ? metrics : Map.of()
        );
    }
}
