package com.aiops.mcp.server;

import com.aiops.mcp.service.MCPService;
import com.aiops.tool.langgraph4j.LangGraphTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MCPToolExecutor {

    private final MCPService mcpService;
    private final ChatLanguageModel chatModel;

    public MCPToolExecutor(MCPService mcpService, ChatLanguageModel chatModel) {
        this.mcpService = mcpService;
        this.chatModel = chatModel;
    }

    public LangGraphTools.ToolResult executeWithLLM(String toolName, String userRequest) {
        try {
            String toolDefs = mcpService.getToolDefinitions();
            
            String prompt = buildToolPrompt(toolName, userRequest, toolDefs);
            String response = chatModel.chat(prompt);
            
            return LangGraphTools.ToolResult.success(toolName, response);
        } catch (Exception e) {
            return LangGraphTools.ToolResult.error(toolName, e.getMessage());
        }
    }

    public LangGraphTools.ToolResult executeDirect(String toolName, Map<String, Object> params) {
        try {
            Map<String, Object> result = mcpService.callTool(toolName, params);
            return LangGraphTools.ToolResult.success(toolName, result.toString());
        } catch (Exception e) {
            return LangGraphTools.ToolResult.error(toolName, e.getMessage());
        }
    }

    private String buildToolPrompt(String toolName, String userRequest, String toolDefs) {
        return String.format("""
            你是一个MCP工具调用助手。请根据用户请求选择合适的工具并生成调用参数。

            可用工具:
            %s

            用户请求: %s

            请选择工具并生成JSON格式的调用参数。格式如下:
            {
                "tool": "工具名称",
                "arguments": {
                    "参数1": "值1",
                    "参数2": "值2"
                }
            }
            """, toolDefs, userRequest);
    }
}
