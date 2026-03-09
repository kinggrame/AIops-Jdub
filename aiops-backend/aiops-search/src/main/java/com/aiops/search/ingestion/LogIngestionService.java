package com.aiops.search.ingestion;

import java.util.List;
import java.util.Map;

public interface LogIngestionService {

    void ingest(String agentId, String hostname, List<Map<String, Object>> logs);
}
