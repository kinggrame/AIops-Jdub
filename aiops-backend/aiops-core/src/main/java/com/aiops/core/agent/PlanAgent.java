package com.aiops.core.agent;

import java.util.Map;

public interface PlanAgent {

    Map<String, Object> plan(String message, Map<String, Object> summary);
}
