package com.aiops.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiops.cache")
public class CacheProperties {

    private long maximumSize = 1000;
    private long expireAfterSeconds = 600;
    private Redis redis = new Redis();

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public long getExpireAfterSeconds() {
        return expireAfterSeconds;
    }

    public void setExpireAfterSeconds(long expireAfterSeconds) {
        this.expireAfterSeconds = expireAfterSeconds;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public static class Redis {
        private boolean enabled = true;
        private String keyPrefix = "aiops:";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }
}
