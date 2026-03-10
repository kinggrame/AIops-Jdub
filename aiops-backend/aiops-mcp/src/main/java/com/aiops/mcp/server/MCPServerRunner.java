package com.aiops.mcp.server;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.*;

@Component
public class MCPServerRunner {
    private final Map<String, MCPToolHandler> tools = new ConcurrentHashMap<>();
    private final Map<String, Object> context = new ConcurrentHashMap<>();

    public void registerTool(String name, String description, MCPToolHandler handler) {
        tools.put(name, handler);
    }

    public List<Map<String, Object>> listTools() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, MCPToolHandler> entry : tools.entrySet()) {
            Map<String, Object> tool = new HashMap<>();
            tool.put("name", entry.getKey());
            tool.put("description", entry.getValue().getDescription());
            tool.put("inputSchema", entry.getValue().getInputSchema());
            result.add(tool);
        }
        return result;
    }

    public Object callTool(String name, Map<String, Object> params) {
        MCPToolHandler handler = tools.get(name);
        if (handler == null) {
            return Map.of("error", "Tool not found: " + name);
        }
        try {
            return handler.execute(params);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    public interface MCPToolHandler {
        Object execute(Map<String, Object> params);
        String getDescription();
        Map<String, Object> getInputSchema();
    }

    public String getToolsJson() {
        List<Map<String, Object>> toolList = listTools();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < toolList.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"name\":\"").append(toolList.get(i).get("name")).append("\",");
            sb.append("\"description\":\"").append(toolList.get(i).get("description")).append("\",");
            sb.append("\"inputSchema\":").append(toolList.get(i).get("inputSchema"));
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
