package com.aiops.web.controller;

import com.aiops.common.model.ApiResponse;
import com.aiops.web.tunnel.AgentInstallCommandService;
import com.aiops.web.tunnel.TunnelLifecycleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class TunnelController {

    private final TunnelLifecycleManager tunnelLifecycleManager;
    private final AgentInstallCommandService agentInstallCommandService;

    public TunnelController(TunnelLifecycleManager tunnelLifecycleManager,
                            AgentInstallCommandService agentInstallCommandService) {
        this.tunnelLifecycleManager = tunnelLifecycleManager;
        this.agentInstallCommandService = agentInstallCommandService;
    }

    @GetMapping("/tunnel")
    public ApiResponse<?> tunnelStatus() {
        return ApiResponse.ok(tunnelLifecycleManager.status());
    }

    @GetMapping("/tunnel/install-preview")
    public ApiResponse<?> installPreview() {
        return ApiResponse.ok(agentInstallCommandService.preview());
    }
}
