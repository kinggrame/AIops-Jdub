package com.aiops.agent.workflow;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LangGraphWorkflowService {
    private final LlmClient llmClient;
    private final ToolExecutorService toolExecutor;
    private final Map<String, WorkflowState> activeWorkflows = new ConcurrentHashMap<>();

    public LangGraphWorkflowService(LlmClient llmClient, ToolExecutorService toolExecutor) {
        this.llmClient = llmClient;
        this.toolExecutor = toolExecutor;
    }

    public interface LlmClient {
        String chat(String prompt);
    }

    public interface ToolExecutorService {
        Object execute(String toolName, Map<String, Object> params);
    }

    public static class WorkflowState {
        private String sessionId;
        private String userRequest;
        private Map<String, Object> context = new ConcurrentHashMap<>();
        private List<StateMessage> messages = new ArrayList<>();
        private String currentNode;
        private int iteration = 0;
        private boolean completed = false;
        private String finalResult;

        public static class StateMessage {
            public String role;
            public String content;
            public String node;
            public long timestamp;

            public StateMessage(String role, String content, String node) {
                this.role = role;
                this.content = content;
                this.node = node;
                this.timestamp = System.currentTimeMillis();
            }
        }

        public void addMessage(String role, String content, String node) {
            messages.add(new StateMessage(role, content, node));
        }

        public String getCompressedHistory(int maxMessages) {
            if (messages.size() <= maxMessages) {
                return messagesToString();
            }
            List<StateMessage> recent = messages.subList(messages.size() - maxMessages, messages.size());
            StringBuilder sb = new StringBuilder();
            sb.append("[早期摘要]\n");
            sb.append(summarize(messages.subList(0, messages.size() - maxMessages)));
            sb.append("\n\n[最近对话]\n");
            for (StateMessage msg : recent) {
                sb.append(msg.node).append(": ").append(msg.content).append("\n");
            }
            return sb.toString();
        }

        private String summarize(List<StateMessage> msgs) {
            if (msgs.isEmpty()) return "";
            Set<String> nodes = new HashSet<>();
            for (StateMessage msg : msgs) nodes.add(msg.node);
            return "经过" + nodes.size() + "个节点处理";
        }

        private String messagesToString() {
            StringBuilder sb = new StringBuilder();
            for (StateMessage msg : messages) {
                sb.append(msg.node).append(": ").append(msg.content).append("\n");
            }
            return sb.toString();
        }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getUserRequest() { return userRequest; }
        public void setUserRequest(String userRequest) { this.userRequest = userRequest; }
        public Map<String, Object> getContext() { return context; }
        public List<StateMessage> getMessages() { return messages; }
        public String getCurrentNode() { return currentNode; }
        public void setCurrentNode(String currentNode) { this.currentNode = currentNode; }
        public int getIteration() { return iteration; }
        public void incrementIteration() { this.iteration++; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public String getFinalResult() { return finalResult; }
        public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
    }

    public String execute(String sessionId, String userRequest) {
        WorkflowState state = new WorkflowState();
        state.setSessionId(sessionId);
        state.setUserRequest(userRequest);
        activeWorkflows.put(sessionId, state);

        try {
            String result = runWorkflow(state);
            state.setFinalResult(result);
            state.setCompleted(true);
            return result;
        } finally {
            activeWorkflows.remove(sessionId);
        }
    }

    private String runWorkflow(WorkflowState state) {
        String plan = executeNode("planner", state, "你是一个Planner Agent。请分析用户请求并制定执行计划。\n用户请求: " + state.getUserRequest());

        if (shouldStop(plan)) return plan;
        state.getContext().put("plan", plan);

        String analysis = executeNode("analyzer", state, "你是一个Analyzer Agent。请分析以下计划:\n" + plan);

        if (shouldStop(analysis)) return analysis;
        state.getContext().put("analysis", analysis);

        String execution = executeNode("executor", state, "你是一个Executor Agent。请执行以下分析:\n" + analysis);

        if (shouldStop(execution)) return execution;
        state.getContext().put("execution", execution);

        String report = executeNode("report", state, "你是一个Report Agent。请生成最终报告:\n" + execution);

        return report;
    }

    private String executeNode(String nodeName, WorkflowState state, String prompt) {
        state.setCurrentNode(nodeName);
        String history = state.getCompressedHistory(10);
        
        String fullPrompt = prompt;
        if (!history.isEmpty()) {
            fullPrompt += "\n\n历史: " + history;
        }

        String response = llmClient.chat(fullPrompt);
        state.addMessage("assistant", response, nodeName);

        List<Map<String, Object>> toolCalls = extractToolCalls(response);
        for (Map<String, Object> call : toolCalls) {
            String toolName = (String) call.get("name");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) call.get("params");
            Object result = toolExecutor.execute(toolName, params);
            state.addMessage("system", "工具[" + toolName + "]执行结果: " + result, nodeName);
        }

        state.incrementIteration();
        return response;
    }

    private List<Map<String, Object>> extractToolCalls(String response) {
        List<Map<String, Object>> calls = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[TOOL:(\\w+):([^\\]]+)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            Map<String, Object> call = new HashMap<>();
            call.put("name", matcher.group(1));
            call.put("params", parseParams(matcher.group(2)));
            calls.add(call);
        }
        return calls;
    }

    private Map<String, Object> parseParams(String paramsStr) {
        Map<String, Object> params = new HashMap<>();
        String[] pairs = paramsStr.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0].trim(), kv[1].trim());
            }
        }
        return params;
    }

    private boolean shouldStop(String response) {
        return response != null && response.contains("[FINAL]");
    }

    public WorkflowState getState(String sessionId) {
        return activeWorkflows.get(sessionId);
    }
}
