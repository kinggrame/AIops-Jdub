package com.aiops.agent.service;

import com.aiops.tool.registry.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ToolExecutionService {

    private final ToolRegistry toolRegistry;

    public ToolExecutionService(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public Object execute(String toolName, Map<String, Object> params) {
        try {
            return toolRegistry.execute(toolName, params);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage(), "tool", toolName);
        }
    }

    public String getToolDefinitions() {
        return toolRegistry.getDefinitions();
    }
}
