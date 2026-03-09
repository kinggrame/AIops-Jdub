package com.aiops.search.ingestion;

import com.aiops.search.service.SearchService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
public class JpaLogIngestionService implements LogIngestionService {

    private final SearchService searchService;

    public JpaLogIngestionService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public void ingest(String agentId, String hostname, List<Map<String, Object>> logs) {
        searchService.indexAgentReport(agentId, hostname, Map.of("logwatch", true), logs);
    }
}
