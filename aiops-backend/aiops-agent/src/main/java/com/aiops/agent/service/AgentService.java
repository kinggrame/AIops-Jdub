package com.aiops.agent.service;

import com.aiops.agent.model.*;
import com.aiops.agent.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentService {
    private final AIAgentRepository agentRepository;
    private final AgentSessionRepository sessionRepository;
    private final AgentMessageRepository messageRepository;
    private final AgentCoordinator coordinator;

    public AgentService(AIAgentRepository agentRepository, 
                       AgentSessionRepository sessionRepository,
                       AgentMessageRepository messageRepository,
                       AgentCoordinator coordinator) {
        this.agentRepository = agentRepository;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.coordinator = coordinator;
    }

    public List<AIAgent> findAllAgents() { return agentRepository.findAll(); }
    public List<AIAgent> findEnabledAgents() { return agentRepository.findByEnabledTrue(); }
    public AIAgent findAgentById(String id) { return agentRepository.findById(id).orElse(null); }
    public AIAgent saveAgent(AIAgent agent) {
        agent.setUpdatedAt(java.time.LocalDateTime.now());
        return agentRepository.save(agent);
    }

    public AgentSession chat(String userId, String message) {
        String sessionId = UUID.randomUUID().toString();
        AgentSession session = new AgentSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setInitialRequest(message);
        sessionRepository.save(session);

        String report = coordinator.coordinate(sessionId, message);
        session.setFinalReport(report);
        session.setStatus("COMPLETED");
        sessionRepository.save(session);
        return session;
    }

    public AgentSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    public List<AgentSession> getUserSessions(String userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AgentMessage> getSessionMessages(String sessionId) {
        return messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    public void initializeDefaultAgents() {
        if (agentRepository.count() == 0) {
            createDefaultAgent("planner", "计划Agent", AIAgent.AgentType.PLANNER,
                "你是一个运维计划Agent，负责分析用户请求并制定执行计划。你需要：1. 理解用户需求 2. 分析需要执行的步骤 3. 选择合适的Tool 4. 生成执行计划",
                List.of("elk_query", "milvus_search", "agent_metrics"));
            createDefaultAgent("analyzer", "分析Agent", AIAgent.AgentType.ANALYZER,
                "你是一个运维分析Agent，负责分析问题并生成解决方案。你需要：1. 收集相关日志和指标 2. 分析问题根因 3. 生成修复脚本 4. 评估解决方案",
                List.of("elk_query", "milvus_search", "agent_metrics", "execute_script"));
            createDefaultAgent("executor", "执行Agent", AIAgent.AgentType.EXECUTOR,
                "你是一个执行Agent，负责执行运维动作。你需要：1. 执行生成的脚本 2. 验证执行结果 3. 处理错误和异常 4. 记录执行日志",
                List.of("execute_script", "agent_metrics"));
            createDefaultAgent("report", "报告Agent", AIAgent.AgentType.REPORT,
                "你是一个报告Agent，负责生成执行报告。你需要：1. 收集执行结果 2. 汇总执行情况 3. 生成可读的报告 4. 提供后续建议",
                List.of());
        }
    }

    private void createDefaultAgent(String id, String name, AIAgent.AgentType type, String prompt, List<String> tools) {
        AIAgent agent = new AIAgent();
        agent.setId(id);
        agent.setName(name);
        agent.setType(type);
        agent.setSystemPrompt(prompt);
        agent.setAvailableTools(tools);
        agent.setEnabled(true);
        agentRepository.save(agent);
    }
}
