package com.aiops.core.agent;

import java.util.List;
import java.util.Map;

public interface AnalysisAgent {

    Map<String, Object> diagnose(String userMessage, Map<String, Object> metrics, List<Map<String, Object>> events);
}
