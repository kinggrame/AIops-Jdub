package com.aiops.command.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "aiops.command")
public class CommandProperties {

    private List<String> allowed = new ArrayList<>(List.of("restart_service", "get_logs", "clear_cache", "scale_deployment"));
    private List<String> forbidden = new ArrayList<>(List.of("rm -rf", "shutdown", "init 6"));

    public List<String> getAllowed() {
        return allowed;
    }

    public void setAllowed(List<String> allowed) {
        this.allowed = allowed;
    }

    public List<String> getForbidden() {
        return forbidden;
    }

    public void setForbidden(List<String> forbidden) {
        this.forbidden = forbidden;
    }
}
