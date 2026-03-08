package com.aiops.search.service;

import com.aiops.search.entity.LogEntry;

import java.util.List;
import java.util.Map;

/**
 * Search service for operational logs.
 *
 * <p>Provides MVP log indexing and retrieval APIs. The current implementation uses
 * in-memory collections and keeps the Elasticsearch boundary behind this interface.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Indexing is synchronous in the MVP.</li>
 *   <li>TODO: batch and asynchronously index logs into Elasticsearch.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface SearchService {

    /**
     * Indexes an incoming agent report for later search.
     *
     * @param agentId agent id
     * @param hostname host name
     * @param metrics metrics payload
     * @param events event payload
     */
    void indexAgentReport(String agentId, String hostname, Map<String, Object> metrics, List<Map<String, Object>> events);

    /**
     * Searches indexed logs.
     *
     * @param query query text
     * @param limit max results
     * @return matched log entries
     */
    List<LogEntry> searchLogs(String query, int limit);
}
