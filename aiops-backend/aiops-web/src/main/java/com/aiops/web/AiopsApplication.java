package com.aiops.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.aiops")
@EnableJpaRepositories(basePackages = "com.aiops")
@EntityScan(basePackages = "com.aiops")
@ConfigurationPropertiesScan(basePackages = "com.aiops")
public class AiopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiopsApplication.class, args);
    }
}
