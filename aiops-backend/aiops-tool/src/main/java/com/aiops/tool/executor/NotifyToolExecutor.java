package com.aiops.tool.executor;

import com.aiops.tool.model.Tool;
import com.aiops.tool.registry.ToolExecutor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class NotifyToolExecutor implements ToolExecutor {
    
    @Override
    public Object execute(Map<String, Object> params) {
        String channel = (String) params.get("channel");
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        
        return Map.of(
            "success", true,
            "channel", channel != null ? channel : "default",
            "title", title,
            "message", "Notification sent: " + title,
            "timestamp", System.currentTimeMillis()
        );
    }

    @Override
    public String getDefinition() {
        return """
        {"type":"function","function":{"name":"notify","description":"发送通知到指定渠道","parameters":{"type":"object","properties":{"channel":{"type":"string","description":"通知渠道: dingtalk/wecom/feishu/email"},"title":{"type":"string","description":"通知标题"},"content":{"type":"string","description":"通知内容"}},"required":["title","content"]}}
        """;
    }

    @Override
    public Tool.ToolType getType() { return Tool.ToolType.NOTIFY; }
}
