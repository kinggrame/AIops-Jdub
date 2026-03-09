package com.aiops.search.service;

import com.aiops.search.entity.LogEntry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ConditionalOnMissingBean(SearchService.class)
public class InMemorySearchService implements SearchService {

    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();

    @Override
    public void indexAgentReport(String agentId, String hostname, Map<String, Object> metrics, List<Map<String, Object>> events) {
        String message = "Metrics report from " + hostname + " metrics=" + metrics + " events=" + events;
        logs.add(new LogEntry(UUID.randomUUID().toString(), agentId, hostname, "INFO", message, Map.of(
                "metrics", metrics,
                "events", events
        ), Instant.now()));
    }

    @Override
    public List<LogEntry> searchLogs(String query, int limit) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return logs.stream()
                .filter(log -> normalized.isBlank()
                        || log.message().toLowerCase(Locale.ROOT).contains(normalized)
                        || log.hostname().toLowerCase(Locale.ROOT).contains(normalized))
                .limit(limit)
                .toList();
    }
}
