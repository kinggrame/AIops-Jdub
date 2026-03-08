package com.aiops.cache.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryCacheService implements CacheService {

    private final Cache<String, Object> localCache;
    private final Map<String, CacheEntry> remoteCache = new ConcurrentHashMap<>();

    public InMemoryCacheService(Cache<String, Object> localCache) {
        this.localCache = localCache;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object local = localCache.getIfPresent(key);
        if (type.isInstance(local)) {
            return Optional.of(type.cast(local));
        }

        CacheEntry entry = remoteCache.get(key);
        if (entry == null || entry.expired()) {
            remoteCache.remove(key);
            return Optional.empty();
        }

        if (type.isInstance(entry.value())) {
            localCache.put(key, entry.value());
            return Optional.of(type.cast(entry.value()));
        }
        return Optional.empty();
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        localCache.put(key, value);
        remoteCache.put(key, new CacheEntry(value, Instant.now().plus(ttl)));
    }

    @Override
    public void delete(String key) {
        localCache.invalidate(key);
        remoteCache.remove(key);
    }

    private record CacheEntry(Object value, Instant expireAt) {
        private boolean expired() {
            return Instant.now().isAfter(expireAt);
        }
    }
}
