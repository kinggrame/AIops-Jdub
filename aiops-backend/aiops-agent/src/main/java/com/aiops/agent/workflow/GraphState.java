package com.aiops.agent.workflow;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GraphState implements Serializable {
    private String sessionId;
    private String userRequest;
    private Map<String, Object> context = new ConcurrentHashMap<>();
    private List<GraphMessage> messages = new ArrayList<>();
    private Map<String, String> agentOutputs = new ConcurrentHashMap<>();
    private List<ToolCall> toolCalls = new ArrayList<>();
    private String currentNode;
    private Map<String, Object> metadata = new ConcurrentHashMap<>();
    private int iteration = 0;
    private boolean completed = false;
    private String finalResult;

    public static class GraphMessage implements Serializable {
        public String role;
        public String content;
        public String agent;
        public long timestamp;

        public GraphMessage(String role, String content, String agent) {
            this.role = role;
            this.content = content;
            this.agent = agent;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class ToolCall implements Serializable {
        public String toolName;
        public Map<String, Object> params;
        public Object result;
        public boolean success;

        public ToolCall(String toolName, Map<String, Object> params) {
            this.toolName = toolName;
            this.params = params;
        }
    }

    public void addMessage(String role, String content, String agent) {
        messages.add(new GraphMessage(role, content, agent));
    }

    public void addToolCall(String toolName, Map<String, Object> params) {
        toolCalls.add(new ToolCall(toolName, params));
    }

    public String getCompressedHistory(int maxMessages) {
        if (messages.size() <= maxMessages) {
            return messagesToString();
        }
        
        List<GraphMessage> recent = messages.subList(messages.size() - maxMessages, messages.size());
        StringBuilder sb = new StringBuilder();
        sb.append("[早期对话摘要]\n");
        sb.append(formatSummary(messages.subList(0, messages.size() - maxMessages)));
        sb.append("\n\n[最近对话]\n");
        for (GraphMessage msg : recent) {
            sb.append(msg.role).append(": ").append(msg.content).append("\n");
        }
        return sb.toString();
    }

    private String formatSummary(List<GraphMessage> msgs) {
        if (msgs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        Set<String> agents = new HashSet<>();
        int toolCalls = 0;
        for (GraphMessage msg : msgs) {
            agents.add(msg.agent);
            if (msg.content.contains("[TOOL:")) toolCalls++;
        }
        sb.append("涉及").append(agents.size()).append("个Agent，");
        sb.append(toolCalls).append("次工具调用");
        return sb.toString();
    }

    private String messagesToString() {
        StringBuilder sb = new StringBuilder();
        for (GraphMessage msg : messages) {
            sb.append(msg.agent).append(" (").append(msg.role).append("): ");
            sb.append(msg.content).append("\n");
        }
        return sb.toString();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserRequest() { return userRequest; }
    public void setUserRequest(String userRequest) { this.userRequest = userRequest; }
    public Map<String, Object> getContext() { return context; }
    public List<GraphMessage> getMessages() { return messages; }
    public Map<String, String> getAgentOutputs() { return agentOutputs; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public String getCurrentNode() { return currentNode; }
    public void setCurrentNode(String currentNode) { this.currentNode = currentNode; }
    public Map<String, Object> getMetadata() { return metadata; }
    public int getIteration() { return iteration; }
    public void incrementIteration() { this.iteration++; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
}
