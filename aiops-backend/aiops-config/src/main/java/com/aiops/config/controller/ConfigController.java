package com.aiops.config.controller;

import com.aiops.config.model.SystemConfig;
import com.aiops.config.service.ConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void init() {
        configService.initializeDefaults();
    }

    @GetMapping
    public List<SystemConfig> list() { return configService.findAll(); }

    @GetMapping("/category/{category}")
    public List<SystemConfig> byCategory(@PathVariable String category) { 
        return configService.findByCategory(category); 
    }

    @GetMapping("/{key}")
    public SystemConfig get(@PathVariable String key) { return configService.findByKey(key); }

    @PutMapping("/{key}")
    public SystemConfig update(@PathVariable String key, @RequestBody Map<String, String> request) {
        return configService.update(key, request.get("value"));
    }

    @PostMapping
    public SystemConfig create(@RequestBody SystemConfig config) { return configService.save(config); }
}
