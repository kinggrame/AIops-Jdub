package com.aiops.connection.service;

import com.aiops.connection.model.CommandResult;

import java.util.List;
import java.util.Map;

/**
 * Command dispatch service.
 *
 * <p>Abstracts command delivery to agent clients and records command execution results.
 * The MVP stores dispatch metadata in memory and simulates queueing for later transport.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Dispatch creation is synchronous in the MVP.</li>
 *   <li>TODO: send commands asynchronously over WebSocket or MQ.</li>
 *   <li>TODO: support callbacks and timeout tracking.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface CommandDispatchService {

    /**
     * Dispatches a command to an agent.
     *
     * @param agentId target agent id
     * @param action command action
     * @param params command params
     * @return dispatch metadata
     */
    Map<String, Object> dispatch(String agentId, String action, Map<String, Object> params);

    /**
     * Records the result of a previously dispatched command.
     *
     * @param result command result
     */
    void recordResult(CommandResult result);

    List<CommandResult> listResults();

    List<Map<String, Object>> listPending(String agentId);
}
