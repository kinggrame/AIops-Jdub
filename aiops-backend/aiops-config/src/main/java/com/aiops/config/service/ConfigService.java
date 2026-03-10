package com.aiops.config.service;

import com.aiops.config.model.SystemConfig;
import com.aiops.config.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ConfigService {
    private final SystemConfigRepository configRepository;

    public ConfigService(SystemConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<SystemConfig> findAll() { return configRepository.findAll(); }
    public SystemConfig findByKey(String key) { return configRepository.findById(key).orElse(null); }
    public List<SystemConfig> findByCategory(String category) { return configRepository.findByCategory(category); }
    
    public SystemConfig save(SystemConfig config) {
        config.setUpdatedAt(java.time.LocalDateTime.now());
        return configRepository.save(config);
    }

    public SystemConfig update(String key, String value) {
        SystemConfig config = findByKey(key);
        if (config == null) {
            throw new RuntimeException("Config not found: " + key);
        }
        if (!config.getEditable()) {
            throw new RuntimeException("Config is not editable: " + key);
        }
        config.setValue(value);
        config.setUpdatedAt(java.time.LocalDateTime.now());
        return configRepository.save(config);
    }

    public String getValue(String key, String defaultValue) {
        SystemConfig config = findByKey(key);
        return config != null ? config.getValue() : defaultValue;
    }

    public void initializeDefaults() {
        if (configRepository.count() == 0) {
            createConfig("system.name", "AIOps", "System name", "system");
            createConfig("system.timezone", "Asia/Shanghai", "System timezone", "system");
            createConfig("alert.enabled", "true", "Enable alert", "alert");
            createConfig("alert.threshold.cpu", "80", "CPU alert threshold", "alert");
            createConfig("alert.threshold.memory", "85", "Memory alert threshold", "alert");
            createConfig("agent.heartbeat.interval", "30", "Agent heartbeat interval (seconds)", "agent");
            createConfig("log.retention.days", "30", "Log retention days", "log");
        }
    }

    private void createConfig(String key, String value, String desc, String category) {
        SystemConfig config = new SystemConfig();
        config.setKey(key);
        config.setValue(value);
        config.setDescription(desc);
        config.setCategory(category);
        configRepository.save(config);
    }
}
