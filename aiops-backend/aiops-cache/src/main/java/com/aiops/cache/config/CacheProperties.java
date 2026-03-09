package com.aiops.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "aiops.cache")
public class CacheProperties {

    private long maximumSize = 1000;
    private long expireAfterSeconds = 600;
    private RedisProperty redis = new RedisProperty();


    @Data
    public static class RedisProperty {
        private boolean enabled = true;
        private String keyPrefix = "aiops:";

    }
}
