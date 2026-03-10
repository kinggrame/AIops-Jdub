package com.aiops.agent.workflow;

import com.aiops.agent.model.AIAgent;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.*;

@Component
public class AgentNode implements GraphNode {
    private final String agentId;
    private final String name;
    private final String systemPrompt;
    private final LlmClient llmClient;
    private final List<String> availableTools;

    public AgentNode(String agentId, String name, String systemPrompt, LlmClient llmClient, List<String> tools) {
        this.agentId = agentId;
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.llmClient = llmClient;
        this.availableTools = tools;
    }

    @Override
    public String getName() {
        return agentId;
    }

    @Override
    public GraphState process(GraphState state) {
        String compressedHistory = state.getCompressedHistory(10);
        
        String prompt = buildPrompt(state, compressedHistory);
        String response = llmClient.chat(prompt);

        state.addMessage("assistant", response, name);
        state.getAgentOutputs().put(agentId, response);

        List<Map<String, Object>> toolCalls = parseToolCalls(response);
        for (Map<String, Object> toolCall : toolCalls) {
            state.addToolCall((String) toolCall.get("name"), (Map<String, Object>) toolCall.get("params"));
        }

        if (response.contains("[FINAL]")) {
            state.setFinalResult(extractFinalResult(response));
            state.setCompleted(true);
        }

        return state;
    }

    private String buildPrompt(GraphState state, String history) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的AI运维助手。\n\n");
        sb.append("系统提示: ").append(systemPrompt).append("\n\n");
        
        if (!history.isEmpty()) {
            sb.append("对话历史:\n").append(history).append("\n\n");
        }
        
        if (state.getContext().containsKey("plan")) {
            sb.append("执行计划:\n").append(state.getContext().get("plan")).append("\n\n");
        }
        
        if (!state.getToolCalls().isEmpty()) {
            sb.append("工具执行结果:\n");
            for (GraphState.ToolCall tc : state.getToolCalls()) {
                sb.append("- ").append(tc.toolName).append(": ").append(tc.result).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("用户请求: ").append(state.getUserRequest()).append("\n");
        sb.append("请给出你的分析和行动。\n");
        
        if (!availableTools.isEmpty()) {
            sb.append("可用工具: ").append(String.join(", ", availableTools)).append("\n");
            sb.append("如需调用工具，请按格式: [TOOL:工具名:参数]\n");
        }
        
        sb.append("如果任务完成，请输出 [FINAL]你的最终结论");
        
        return sb.toString();
    }

    private List<Map<String, Object>> parseToolCalls(String response) {
        List<Map<String, Object>> calls = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[TOOL:(\\w+):([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(response);
        
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

    private String extractFinalResult(String response) {
        return response.replaceAll(".*\\[FINAL\\]", "").trim();
    }

    public interface LlmClient {
        String chat(String prompt);
    }
}
