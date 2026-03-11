package com.aiops.agent.service;

import com.aiops.agent.model.*;
import com.aiops.agent.repository.*;
import com.aiops.agent.workflow.LangGraph4jWorkflowService;
import com.aiops.llm.service.LlmService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgentCoordinator {
    private final AIAgentRepository agentRepository;
    private final AgentMessageRepository messageRepository;
    private final ToolExecutionService toolExecutionService;
    private final LlmService llmService;
    private LangGraph4jWorkflowService langGraphService;

    public AgentCoordinator(AIAgentRepository agentRepository,
                          AgentMessageRepository messageRepository,
                          ToolExecutionService toolExecutionService,
                          LlmService llmService) {
        this.agentRepository = agentRepository;
        this.messageRepository = messageRepository;
        this.toolExecutionService = toolExecutionService;
        this.llmService = llmService;
    }

    public String coordinate(String sessionId, String userRequest) {
        try {
            String response = llmService.chat(userRequest, null);
            return response != null ? response : "No response from LLM";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getToolDefinitions() {
        return toolExecutionService.getToolDefinitions();
    }
}
