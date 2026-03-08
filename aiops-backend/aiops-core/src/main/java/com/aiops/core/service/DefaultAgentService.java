package com.aiops.core.service;

import com.aiops.core.agent.AgentFactory;
import com.aiops.core.llm.LlmProvider;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DefaultAgentService implements AgentService {

    private final AgentFactory agentFactory;
    private final List<LlmProvider> providers;

    public DefaultAgentService(AgentFactory agentFactory, List<LlmProvider> providers) {
        this.agentFactory = agentFactory;
        this.providers = providers;
    }

    @Override
    public AgentChatResult chat(AgentChatCommand command) {
        agentFactory.validateType(command.agentType());

        Map<String, Object> details = new LinkedHashMap<>();
        if ("data".equalsIgnoreCase(command.agentType())) {
            details.putAll(agentFactory.dataAgent().analyzeMetrics(command.metrics()));
        } else {
            details.putAll(agentFactory.analysisAgent().diagnose(command.message(), command.metrics(), command.events()));
        }

        if ("report".equalsIgnoreCase(command.agentType())) {
            details.put("summary", agentFactory.reportAgent().buildSummary(details));
        }

        LlmProvider provider = providers.stream()
                .filter(LlmProvider::available)
                .findFirst()
                .orElse(providers.get(providers.size() - 1));
        String providerReply = provider.generate(command.message(), details);
        String reply = "report".equalsIgnoreCase(command.agentType())
                ? String.valueOf(details.getOrDefault("summary", providerReply))
                : providerReply;

        return new AgentChatResult(
                command.conversationId() == null || command.conversationId().isBlank() ? UUID.randomUUID().toString() : command.conversationId(),
                command.agentType(),
                provider.name(),
                reply,
                details
        );
    }
}
