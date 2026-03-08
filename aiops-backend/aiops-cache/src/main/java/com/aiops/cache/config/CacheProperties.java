package com.aiops.cache.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiops.cache")
public class CacheProperties {

    private long maximumSize = 1000;
    private long expireAfterSeconds = 600;

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
}
