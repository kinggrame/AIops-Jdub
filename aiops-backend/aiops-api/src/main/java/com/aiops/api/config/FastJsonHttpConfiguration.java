package com.aiops.api.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class FastJsonHttpConfiguration {
    @Bean
    @Primary
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();

        fastJsonConfig.setCharset(StandardCharsets.UTF_8);
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");

        // Fastjson2 使用 setWriterFeatures(),配置序列化选项
        fastJsonConfig.setWriterFeatures(
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteBigDecimalAsPlain,
                JSONWriter.Feature.WriteLongAsString); // 核心：解决 JavaScript Long 精度丢失问题

        // 如果需要配置反序列化特性，可以使用 setReaderFeatures()
        fastJsonConfig.setReaderFeatures(
                JSONReader.Feature.SupportArrayToBean

                // ... 其他反序列化特性
        );

        fastConverter.setFastJsonConfig(fastJsonConfig);

        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.parseMediaType("application/json;charset=UTF-8"));

        fastConverter.setSupportedMediaTypes(supportedMediaTypes);

        return new HttpMessageConverters(fastConverter);
    }
}
