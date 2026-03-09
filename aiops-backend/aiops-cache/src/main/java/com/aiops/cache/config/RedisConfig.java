package com.aiops.cache.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.data.redis.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. 使用 Fastjson2 序列化 Value
        FastJsonRedisSerializer<Object> serializer = new FastJsonRedisSerializer<>(Object.class);

        // 2. 配置 Fastjson2 的安全特性 (可选但建议)用于支持多态（反序列化回具体子类）
        FastJsonConfig config = new FastJsonConfig();
        // 关键配置：写入时带上类型信息，反序列化时支持 AutoType
        config.setWriterFeatures(JSONWriter.Feature.WriteClassName);
        config.setReaderFeatures(
                        JSONReader.Feature.SupportArrayToBean,
                        JSONReader.Feature.ErrorOnNoneSerializable
                );

        serializer.setFastJsonConfig(config);
        // 注意：生产环境建议开启安全白名单

        // 3. 设置 Key 的序列化器为 String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 4. 设置 Value 的序列化器为 Fastjson2
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
