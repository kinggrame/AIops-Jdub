package com.aiops.tool.executor;

import com.aiops.tool.model.Tool;
import com.aiops.tool.registry.ToolExecutor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ScriptToolExecutor implements ToolExecutor {
    @Override
    public Object execute(Map<String, Object> params) {
        String script = (String) params.get("script");
        Long serverId = (Long) params.get("server_id");
        // Placeholder: 实际实现应该调用Agent执行
        return Map.of("success", true, "output", "Script executed on server " + serverId + ": " + script);
    }

    @Override
    public String getDefinition() {
        return """
        {"type":"function","function":{"name":"execute_script","description":"在目标服务器执行Shell脚本","parameters":{"type":"object","properties":{"script":{"type":"string","description":"要执行的Shell命令"},"server_id":{"type":"integer","description":"目标服务器ID"}},"required":["script","server_id"]}}
        """;
    }

    @Override
    public Tool.ToolType getType() { return Tool.ToolType.SCRIPT; }
}
