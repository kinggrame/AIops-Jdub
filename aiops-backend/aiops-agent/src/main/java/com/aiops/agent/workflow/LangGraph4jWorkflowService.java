package com.aiops.agent.workflow;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Service
public class LangGraph4jWorkflowService {

    public interface PlannerAgent {
        @UserMessage("Analyze this user request and create an execution plan: {{request}}")
        String plan(String request);
    }

    public interface AnalyzerAgent {
        @UserMessage("Analyze this plan and identify potential issues: {{plan}}")
        String analyze(String plan);
    }

    public interface ExecutorAgent {
        @UserMessage("Execute the following analysis and return results: {{analysis}}")
        String execute(String analysis);
    }

    public interface ReportAgent {
        @UserMessage("Generate a final report from this execution: {{execution}}")
        String report(String execution);
    }

    public static class ToolCall {
        private String name;
        private Map<String, Object> arguments;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, Object> getArguments() { return arguments; }
        public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
    }

    public static class AgentState {
        private String sessionId;
        private String userRequest;
        private String plan;
        private String analysis;
        private String execution;
        private String finalReport;
        private boolean completed = false;
        private int iteration = 0;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getUserRequest() { return userRequest; }
        public void setUserRequest(String userRequest) { this.userRequest = userRequest; }
        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; }
        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }
        public String getExecution() { return execution; }
        public void setExecution(String execution) { this.execution = execution; }
        public String getFinalReport() { return finalReport; }
        public void setFinalReport(String finalReport) { this.finalReport = finalReport; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public int getIteration() { return iteration; }
        public void incrementIteration() { this.iteration++; }
    }

    public interface ToolExecutor {
        Object execute(String toolName, Map<String, Object> params);
    }

    private final Map<String, AgentState> activeStates = new java.util.concurrent.ConcurrentHashMap<>();
    private PlannerAgent plannerAgent;
    private AnalyzerAgent analyzerAgent;
    private ExecutorAgent executorAgent;
    private ReportAgent reportAgent;
    private ToolExecutor toolExecutor;

    public void initialize(
            dev.langchain4j.model.chat.ChatLanguageModel chatModel,
            ToolExecutor toolExecutor) {
        
        this.plannerAgent = AiServices.builder(PlannerAgent.class)
                .chatLanguageModel(chatModel)
                .build();

        this.analyzerAgent = AiServices.builder(AnalyzerAgent.class)
                .chatLanguageModel(chatModel)
                .build();

        this.executorAgent = AiServices.builder(ExecutorAgent.class)
                .chatLanguageModel(chatModel)
                .build();

        this.reportAgent = AiServices.builder(ReportAgent.class)
                .chatLanguageModel(chatModel)
                .build();

        this.toolExecutor = toolExecutor;
    }

    public String execute(String sessionId, String userRequest) {
        AgentState state = new AgentState();
        state.setSessionId(sessionId);
        state.setUserRequest(userRequest);
        activeStates.put(sessionId, state);

        try {
            String plan = plannerAgent.plan(userRequest);
            if (shouldStop(plan)) return plan;
            state.setPlan(plan);

            String analysis = analyzerAgent.analyze(plan);
            if (shouldStop(analysis)) return analysis;
            state.setAnalysis(analysis);

            String execution = executorAgent.execute(analysis);
            if (shouldStop(execution)) return execution;
            state.setExecution(execution);

            String report = reportAgent.report(execution);
            state.setFinalReport(report);
            state.setCompleted(true);

            return report;
        } finally {
            activeStates.remove(sessionId);
        }
    }

    private boolean shouldStop(String response) {
        return response != null && response.contains("[FINAL]");
    }

    public AgentState getState(String sessionId) {
        return activeStates.get(sessionId);
    }
}
