package com.aiops.mcp.server;

import com.aiops.mcp.service.MCPService;
import com.aiops.llm.service.LlmService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MCPToolExecutor {

    private final MCPService mcpService;
    private final LlmService llmService;

    public MCPToolExecutor(MCPService mcpService, LlmService llmService) {
        this.mcpService = mcpService;
        this.llmService = llmService;
    }

    public Map<String, Object> executeWithLLM(String toolName, String userRequest) {
        try {
            String toolDefs = mcpService.getToolDefinitions();
            String prompt = buildToolPrompt(toolName, userRequest, toolDefs);
            String response = llmService.chat(prompt, null);
            return Map.of("success", true, "toolName", toolName, "response", response);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> executeDirect(String toolName, Map<String, Object> params) {
        try {
            Map<String, Object> result = mcpService.callTool(toolName, params);
            return Map.of("success", true, "toolName", toolName, "result", result.toString());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private String buildToolPrompt(String toolName, String userRequest, String toolDefs) {
        return String.format("""
            你是一个MCP工具调用助手。请根据用户请求选择合适的工具并生成调用参数。

            可用工具:
            %s

            用户请求: %s

            请选择工具并生成JSON格式的调用参数。
            """, toolDefs, userRequest);
    }
}
