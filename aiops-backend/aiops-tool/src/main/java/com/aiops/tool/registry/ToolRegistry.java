package com.aiops.tool.registry;

import com.aiops.tool.model.Tool;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ToolRegistry {
    private final Map<String, ToolExecutor> executors = new HashMap<>();

    public void register(String toolId, ToolExecutor executor) {
        executors.put(toolId, executor);
    }

    public Object execute(String toolId, Map<String, Object> params) {
        ToolExecutor executor = executors.get(toolId);
        if (executor == null) {
            throw new RuntimeException("Tool not found: " + toolId);
        }
        return executor.execute(params);
    }

    public String getDefinitions() {
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (ToolExecutor e : executors.values()) {
            if (i > 0) sb.append(",");
            sb.append(e.getDefinition());
            i++;
        }
        sb.append("]");
        return sb.toString();
    }

    public Collection<ToolExecutor> getAllExecutors() {
        return executors.values();
    }
}
