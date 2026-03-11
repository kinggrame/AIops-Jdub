package com.aiops.tool.config;

import com.aiops.tool.executor.*;
import com.aiops.tool.registry.ToolRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolRegistryInitializer {

    private final ToolRegistry toolRegistry;
    private final List<ScriptToolExecutor> scriptExecutors;
    private final List<ELKToolExecutor> elkExecutors;
    private final List<MilvusToolExecutor> milvusExecutors;
    private final List<AgentToolExecutor> agentExecutors;
    private final List<NotifyToolExecutor> notifyExecutors;

    public ToolRegistryInitializer(
            ToolRegistry toolRegistry,
            List<ScriptToolExecutor> scriptExecutors,
            List<ELKToolExecutor> elkExecutors,
            List<MilvusToolExecutor> milvusExecutors,
            List<AgentToolExecutor> agentExecutors,
            List<NotifyToolExecutor> notifyExecutors) {
        this.toolRegistry = toolRegistry;
        this.scriptExecutors = scriptExecutors;
        this.elkExecutors = elkExecutors;
        this.milvusExecutors = milvusExecutors;
        this.agentExecutors = agentExecutors;
        this.notifyExecutors = notifyExecutors;
    }

    @PostConstruct
    public void registerTools() {
        scriptExecutors.forEach(executor -> {
            toolRegistry.register(executor.getToolName(), executor);
            System.out.println("Registered tool: " + executor.getToolName());
        });

        elkExecutors.forEach(executor -> {
            toolRegistry.register(executor.getToolName(), executor);
            System.out.println("Registered tool: " + executor.getToolName());
        });

        milvusExecutors.forEach(executor -> {
            toolRegistry.register(executor.getToolName(), executor);
            System.out.println("Registered tool: " + executor.getToolName());
        });

        agentExecutors.forEach(executor -> {
            toolRegistry.register(executor.getToolName(), executor);
            System.out.println("Registered tool: " + executor.getToolName());
        });

        notifyExecutors.forEach(executor -> {
            toolRegistry.register(executor.getToolName(), executor);
            System.out.println("Registered tool: " + executor.getToolName());
        });

        System.out.println("Tool registry initialized with " + toolRegistry.getAllExecutors().size() + " tools");
    }
}
