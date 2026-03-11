package com.aiops.tool.registry;

import com.aiops.tool.model.Tool;
import java.util.*;

public interface ToolExecutor {
    Object execute(Map<String, Object> params);
    String getDefinition();
    Tool.ToolType getType();
    String getToolName();
}
