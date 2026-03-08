package com.aiops.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aiops")
public class AiopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiopsApplication.class, args);
    }
}
