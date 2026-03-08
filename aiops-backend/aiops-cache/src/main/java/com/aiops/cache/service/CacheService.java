package com.aiops.cache.service;

import java.time.Duration;
import java.util.Optional;

/**
 * Dual-layer cache service.
 *
 * <p>Provides an MVP abstraction for local cache plus distributed cache semantics.
 * Current implementation keeps both layers in memory so the backend flow can run
 * without Redis.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Read path is synchronous in the MVP.</li>
 *   <li>Write path is synchronous in the MVP.</li>
 *   <li>TODO: make remote cache synchronization asynchronous after Redis integration.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface CacheService {

    /**
     * Gets a cached value by key.
     *
     * @param key cache key
     * @param type target type
     * @return cached value if present
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Stores a cache value with ttl.
     *
     * @param key cache key
     * @param value cache value
     * @param ttl expiration duration
     */
    void set(String key, Object value, Duration ttl);

    /**
     * Deletes a cache entry.
     *
     * @param key cache key
     */
    void delete(String key);
}
