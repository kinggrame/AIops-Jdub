package com.aiops.command.service;

import com.aiops.command.security.SecurityValidator;
import com.aiops.connection.service.CommandDispatchService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultCommandService implements CommandService {

    private final SecurityValidator securityValidator;
    private final CommandDispatchService commandDispatchService;

    public DefaultCommandService(SecurityValidator securityValidator, CommandDispatchService commandDispatchService) {
        this.securityValidator = securityValidator;
        this.commandDispatchService = commandDispatchService;
    }

    @Override
    public Map<String, Object> dispatchToAgent(String agentId, String action, Map<String, Object> params) {
        securityValidator.validateAction(action);
        securityValidator.validateParams(params);
        return commandDispatchService.dispatch(agentId, action, params);
    }
}
