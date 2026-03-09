package com.aiops.search.service;

import com.aiops.search.entity.LogEntry;
import com.aiops.search.entity.LogEntryEntity;
import com.aiops.search.repository.LogEntryRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
public class JpaSearchService implements SearchService {

    private final LogEntryRepository logEntryRepository;

    public JpaSearchService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @Override
    public void indexAgentReport(String agentId, String hostname, Map<String, Object> metrics, List<Map<String, Object>> events) {
        String message = "Metrics report from " + hostname + " metrics=" + metrics + " events=" + events;
        LogEntryEntity entity = new LogEntryEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAgentId(agentId);
        entity.setHostname(hostname);
        entity.setLevel("INFO");
        entity.setMessage(message);
        entity.setMetadata("metrics=" + metrics + ", events=" + events);
        entity.setTimestamp(Instant.now());
        logEntryRepository.save(entity);
    }

    @Override
    public List<LogEntry> searchLogs(String query, int limit) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return logEntryRepository.findAll().stream()
                .map(this::toDomain)
                .filter(log -> normalized.isBlank()
                        || log.message().toLowerCase(Locale.ROOT).contains(normalized)
                        || log.hostname().toLowerCase(Locale.ROOT).contains(normalized))
                .limit(limit)
                .toList();
    }

    private LogEntry toDomain(LogEntryEntity entity) {
        return new LogEntry(
                entity.getId(),
                entity.getAgentId(),
                entity.getHostname(),
                entity.getLevel(),
                entity.getMessage(),
                entity.getMetadata() == null ? Map.of() : Map.of("raw", entity.getMetadata()),
                entity.getTimestamp()
        );
    }
}
