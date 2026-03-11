package com.aiops.tool.langgraph4j;

import java.lang.annotation.*;
import java.util.function.Function;

public class LangGraphTools {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AiOpsTool {
        String name();
        String description();
    }

    public interface ToolExecutor {
        Object execute(String toolName, String arguments);
    }

    public static class ToolResult {
        private final String toolName;
        private final String result;
        private final boolean success;
        private final String error;

        public ToolResult(String toolName, String result, boolean success, String error) {
            this.toolName = toolName;
            this.result = result;
            this.success = success;
            this.error = error;
        }

        public static ToolResult success(String toolName, String result) {
            return new ToolResult(toolName, result, true, null);
        }

        public static ToolResult error(String toolName, String error) {
            return new ToolResult(toolName, null, false, error);
        }

        public String getToolName() { return toolName; }
        public String getResult() { return result; }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }

    public static class ToolCallRequest {
        private String toolName;
        private String arguments;

        public String getToolName() { return toolName; }
        public void setToolName(String toolName) { this.toolName = toolName; }
        public String getArguments() { return arguments; }
        public void setArguments(String arguments) { this.arguments = arguments; }
    }
}
