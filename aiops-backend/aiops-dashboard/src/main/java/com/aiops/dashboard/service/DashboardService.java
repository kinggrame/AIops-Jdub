package com.aiops.dashboard.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DashboardService {

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("servers", Map.of("total", 0, "online", 0, "offline", 0));
        stats.put("alerts", Map.of("total", 0, "critical", 0, "warning", 0, "info", 0));
        stats.put("actions", Map.of("total", 0, "success", 0, "failed", 0));
        stats.put("agents", Map.of("total", 0, "active", 0));
        stats.put("knowledge", Map.of("repos", 0, "documents", 0));
        
        return stats;
    }

    public Map<String, Object> getTrend(String period) {
        Map<String, Object> trend = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", "2025-03-" + String.format("%02d", 9 - i));
            day.put("alerts", (int) (Math.random() * 10));
            day.put("actions", (int) (Math.random() * 5));
            data.add(day);
        }
        
        trend.put("period", period);
        trend.put("data", data);
        return trend;
    }
}
