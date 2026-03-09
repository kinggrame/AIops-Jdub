package com.aiops.core.agent;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultSummarizeAgent implements SummarizeAgent {

    @Override
    public Map<String, Object> summarize(String message, Map<String, Object> metrics, List<Map<String, Object>> events, List<Map<String, Object>> logs) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("metrics", metrics);
        result.put("events", events);
        result.put("logCount", logs.size());
        result.put("focus", cpu(metrics) >= 90 ? "cpu_hotspot" : "general_investigation");
        result.put("summary", "Compressed telemetry into structured incident context");
        return result;
    }

    private double cpu(Map<String, Object> metrics) {
        Object value = metrics.get("cpu");
        if (value instanceof Map<?, ?> map && map.get("usage") instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }
}
