package com.aiops.core.agent;

import java.util.Map;

public interface ExecuteAgent {

    Map<String, Object> propose(String agentId, String message, Map<String, Object> plan);
}
