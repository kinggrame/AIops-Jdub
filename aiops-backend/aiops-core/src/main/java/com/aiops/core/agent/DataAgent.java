package com.aiops.core.agent;

import java.util.Map;

public interface DataAgent {

    Map<String, Object> analyzeMetrics(Map<String, Object> metrics);
}
