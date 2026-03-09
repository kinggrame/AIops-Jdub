package com.aiops.search.ingestion;

import com.aiops.search.service.SearchService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnMissingBean(LogIngestionService.class)
public class InMemoryLogIngestionService implements LogIngestionService {

    private final SearchService searchService;

    public InMemoryLogIngestionService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public void ingest(String agentId, String hostname, List<Map<String, Object>> logs) {
        searchService.indexAgentReport(agentId, hostname, Map.of("logwatch", true), logs);
    }
}
