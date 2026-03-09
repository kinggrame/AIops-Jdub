package com.aiops.core.agent;

import java.util.List;
import java.util.Map;

public interface SummarizeAgent {

    Map<String, Object> summarize(String message, Map<String, Object> metrics, List<Map<String, Object>> events, List<Map<String, Object>> logs);
}
