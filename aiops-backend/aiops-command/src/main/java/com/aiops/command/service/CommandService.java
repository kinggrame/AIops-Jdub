package com.aiops.command.service;

import java.util.Map;

/**
 * Secure command service.
 *
 * <p>Performs whitelist and parameter validation before delegating commands to the
 * connection layer for agent-side execution.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Validation is synchronous in the MVP.</li>
 *   <li>Dispatch metadata is returned immediately.</li>
 *   <li>TODO: execute long-running tasks asynchronously and stream progress.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface CommandService {

    /**
     * Dispatches a validated command to a target agent.
     *
     * @param agentId target agent id
     * @param action command action
     * @param params command parameters
     * @return dispatch metadata
     */
    Map<String, Object> dispatchToAgent(String agentId, String action, Map<String, Object> params);
}
