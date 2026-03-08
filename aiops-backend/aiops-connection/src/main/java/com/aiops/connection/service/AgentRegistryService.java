package com.aiops.connection.service;

import com.aiops.connection.model.AgentInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agent registry service.
 *
 * <p>Manages server-side registration state for agent clients, including identity,
 * capabilities, last heartbeat time and latest metrics snapshot.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Registration and heartbeat handling are synchronous in the MVP.</li>
 *   <li>TODO: persist agent state asynchronously to database and cache layers.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface AgentRegistryService {

    /**
     * Registers an agent.
     *
     * @param hostname host name
     * @param ip host ip
     * @param token authentication token
     * @param capabilities supported capabilities
     * @return registered agent info
     */
    AgentInfo register(String hostname, String ip, String token, List<String> capabilities);

    /**
     * Finds an agent by id.
     *
     * @param agentId agent id
     * @return optional agent info
     */
    Optional<AgentInfo> findByAgentId(String agentId);

    Optional<AgentInfo> findByHostname(String hostname);

    List<AgentInfo> listAgents();

    /**
     * Updates heartbeat and latest metrics.
     *
     * @param agentId agent id
     * @param metrics latest metrics
     */
    void heartbeat(String agentId, Map<String, Object> metrics);
}
