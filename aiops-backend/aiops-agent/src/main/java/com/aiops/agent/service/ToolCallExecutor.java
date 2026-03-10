package com.aiops.agent.service;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ToolCallExecutor {
    public Object execute(String toolName, Map<String, Object> params) {
        return Map.of("result", "Tool execution placeholder", "tool", toolName);
    }
}
