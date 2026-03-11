package com.aiops.tool.executor;

import com.aiops.tool.model.Tool;
import com.aiops.tool.registry.ToolExecutor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AgentToolExecutor implements ToolExecutor {
    
    @Override
    public Object execute(Map<String, Object> params) {
        String command = (String) params.get("command");
        String agentId = (String) params.get("agent_id");
        
        return Map.of(
            "success", true,
            "agent_id", agentId != null ? agentId : "default",
            "command", command,
            "output", "Command sent to agent: " + command,
            "timestamp", System.currentTimeMillis()
        );
    }

    @Override
    public String getDefinition() {
        return """
        {"type":"function","function":{"name":"agent_execute","description":"在目标Agent上执行命令","parameters":{"type":"object","properties":{"agent_id":{"type":"string","description":"目标Agent ID"},"command":{"type":"string","description":"要执行的命令"}},"required":["command"]}}
        """;
    }

    @Override
    public Tool.ToolType getType() { return Tool.ToolType.AGENT; }

    @Override
    public String getToolName() { return "agent_execute"; }
}
