package com.aiops.agent.service;

import com.aiops.agent.model.*;
import com.aiops.agent.repository.*;
import com.aiops.agent.workflow.LangGraph4jWorkflowService;
import com.aiops.tool.registry.ToolRegistry;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

@Service
public class AgentCoordinator {
    private final AIAgentRepository agentRepository;
    private final AgentMessageRepository messageRepository;
    private final ToolExecutionService toolExecutionService;
    private final ChatLanguageModel chatModel;
    private LangGraph4jWorkflowService langGraphService;

    public AgentCoordinator(AIAgentRepository agentRepository,
                          AgentMessageRepository messageRepository,
                          ToolExecutionService toolExecutionService,
                          ChatLanguageModel chatModel) {
        this.agentRepository = agentRepository;
        this.messageRepository = messageRepository;
        this.toolExecutionService = toolExecutionService;
        this.chatModel = chatModel;
        
        this.langGraphService = new LangGraph4jWorkflowService();
        this.langGraphService.initialize(chatModel, toolExecutionService::execute);
    }

    public String coordinate(String sessionId, String userRequest) {
        try {
            return langGraphService.execute(sessionId, userRequest);
        } catch (Exception e) {
            return "Error executing workflow: " + e.getMessage();
        }
    }

    public String processWithLLM(String prompt) {
        return chatModel.chat(prompt);
    }

    public String getToolDefinitions() {
        return toolExecutionService.getToolDefinitions();
    }
}
