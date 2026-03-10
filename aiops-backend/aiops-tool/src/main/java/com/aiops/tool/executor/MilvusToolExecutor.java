package com.aiops.tool.executor;

import com.aiops.tool.model.Tool;
import com.aiops.tool.registry.ToolExecutor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class MilvusToolExecutor implements ToolExecutor {
    @Override
    public Object execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        Long repoId = (Long) params.get("repo_id");
        Integer topK = (Integer) params.getOrDefault("top_k", 5);
        // Placeholder: 实际实现应该调用Milvus
        return Map.of("success", true, "results", new Object[]{}, "query", query);
    }

    @Override
    public String getDefinition() {
        return """
        {"type":"function","function":{"name":"milvus_search","description":"搜索知识库","parameters":{"type":"object","properties":{"query":{"type":"string","description":"搜索内容"},"repo_id":{"type":"integer","description":"知识库ID"}},"required":["query","repo_id"]}}
        """;
    }

    @Override
    public Tool.ToolType getType() { return Tool.ToolType.MILVUS; }
}
