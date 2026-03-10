package com.aiops.dashboard.controller;

import com.aiops.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return dashboardService.getStats(); }

    @GetMapping("/trend")
    public Map<String, Object> trend(@RequestParam(defaultValue = "7d") String period) {
        return dashboardService.getTrend(period);
    }
}
