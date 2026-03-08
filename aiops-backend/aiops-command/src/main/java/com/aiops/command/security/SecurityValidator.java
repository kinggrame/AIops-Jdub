package com.aiops.command.security;

import com.aiops.command.config.CommandProperties;
import com.aiops.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityValidator {

    private final CommandProperties properties;

    public SecurityValidator(CommandProperties properties) {
        this.properties = properties;
    }

    public void validateAction(String action) {
        if (properties.getAllowed().stream().noneMatch(action::equalsIgnoreCase)) {
            throw new BusinessException("Command not allowed: " + action);
        }
        if (properties.getForbidden().stream().anyMatch(forbidden -> action.toLowerCase().contains(forbidden.toLowerCase()))) {
            throw new BusinessException("Dangerous command blocked: " + action);
        }
    }

    public void validateParams(Map<String, Object> params) {
        for (Object value : params.values()) {
            String text = String.valueOf(value);
            if (text.contains(";") || text.contains("&&") || text.contains("|") || text.contains("`")) {
                throw new BusinessException("Potential command injection detected");
            }
        }
    }
}
