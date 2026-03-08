package com.aiops.core.service;

/**
 * Agent report flow service.
 *
 * <p>Coordinates the main closed-loop workflow for the backend MVP:</p>
 * <ol>
 *   <li>accept telemetry from the agent</li>
 *   <li>evaluate alerts</li>
 *   <li>run analysis with knowledge and logs</li>
 *   <li>optionally prepare a command dispatch</li>
 * </ol>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>The full pipeline is synchronous in the MVP.</li>
 *   <li>TODO: split storage, analysis and dispatch into asynchronous stages.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface AgentReportFlowService {

    /**
     * Processes a full agent report pipeline.
     *
     * @param command reported agent payload
     * @return pipeline result
     */
    AgentReportResult process(AgentReportCommand command);
}
