package com.aiops.core.agent;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultDataAgent implements DataAgent {

    @Override
    public Map<String, Object> analyzeMetrics(Map<String, Object> metrics) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("metrics", metrics);
        summary.put("risk", calculateRisk(metrics));
        return summary;
    }

    private String calculateRisk(Map<String, Object> metrics) {
        double cpu = nested(metrics, "cpu", "usage");
        double memory = nested(metrics, "memory", "usage");
        if (cpu >= 95 || memory >= 90) {
            return "critical";
        }
        if (cpu >= 85 || memory >= 80) {
            return "warning";
        }
        return "normal";
    }

    private double nested(Map<String, Object> metrics, String group, String key) {
        Object value = metrics.get(group);
        if (value instanceof Map<?, ?> map && map.get(key) instanceof Number number) {
            return number.doubleValue();
        }
        return 0;
    }
}
