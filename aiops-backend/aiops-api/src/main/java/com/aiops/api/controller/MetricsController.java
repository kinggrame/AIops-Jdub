package com.aiops.api.controller;

import com.aiops.api.dto.request.LogSearchRequest;
import com.aiops.common.model.ApiResponse;
import com.aiops.connection.service.AgentRegistryService;
import com.aiops.search.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final AgentRegistryService agentRegistryService;
    private final SearchService searchService;

    public MetricsController(AgentRegistryService agentRegistryService, SearchService searchService) {
        this.agentRegistryService = agentRegistryService;
        this.searchService = searchService;
    }

    @GetMapping("/agents")
    public ApiResponse<?> latestMetrics() {
        return ApiResponse.ok(agentRegistryService.listAgents());
    }

    @PostMapping("/logs/search")
    public ApiResponse<?> searchLogs(@Valid @RequestBody LogSearchRequest request) {
        int limit = request.limit() <= 0 ? 10 : request.limit();
        return ApiResponse.ok(searchService.searchLogs(request.query(), limit));
    }
}
