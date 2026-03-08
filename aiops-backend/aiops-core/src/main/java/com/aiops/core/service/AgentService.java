package com.aiops.core.service;

/**
 * Agent conversation service.
 *
 * <p>Provides the entry point for multi-agent analysis and report generation. The MVP
 * performs synchronous processing and provider fallback in-process.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Chat handling is synchronous in the MVP.</li>
 *   <li>TODO: support CompletableFuture and streaming responses.</li>
 *   <li>TODO: route heavy analysis via MQ-based background processing.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface AgentService {

    /**
     * Processes an agent chat command.
     *
     * @param command chat command
     * @return chat result
     */
    AgentChatResult chat(AgentChatCommand command);
}
