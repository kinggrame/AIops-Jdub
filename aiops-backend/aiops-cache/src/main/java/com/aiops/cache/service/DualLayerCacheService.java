package com.aiops.cache.service;

import com.aiops.cache.config.CacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class DualLayerCacheService implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(DualLayerCacheService.class);

    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;

    public DualLayerCacheService(Cache<String, Object> localCache,
                                 RedisTemplate<String, Object> redisTemplate,
                                 CacheProperties properties) {
        this.localCache = localCache;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object local = localCache.getIfPresent(key);
        if (type.isInstance(local)) {
            return Optional.of(type.cast(local));
        }

        if (!properties.getRedis().isEnabled()) {
            return Optional.empty();
        }

        try {
            Object remote = redisTemplate.opsForValue().get(redisKey(key));
            if (type.isInstance(remote)) {
                localCache.put(key, remote);
                return Optional.of(type.cast(remote));
            }
        } catch (DataAccessException exception) {
            log.warn("Redis cache read failed, fallback to caffeine only: {}", exception.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        localCache.put(key, value);

        if (!properties.getRedis().isEnabled()) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(redisKey(key), value, ttl);
        } catch (DataAccessException exception) {
            log.warn("Redis cache write failed, keep caffeine entry only: {}", exception.getMessage());
        }
    }

    @Override
    public void delete(String key) {
        localCache.invalidate(key);

        if (!properties.getRedis().isEnabled()) {
            return;
        }

        try {
            redisTemplate.delete(redisKey(key));
        } catch (DataAccessException exception) {
            log.warn("Redis cache delete failed: {}", exception.getMessage());
        }
    }

    private String redisKey(String key) {
        return properties.getRedis().getKeyPrefix() + key;
    }
}
