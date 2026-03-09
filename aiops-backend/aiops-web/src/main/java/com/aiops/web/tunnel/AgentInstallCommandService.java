package com.aiops.web.tunnel;

import org.springframework.stereotype.Service;

@Service
public class AgentInstallCommandService {

    private final TunnelLifecycleManager tunnelLifecycleManager;

    public AgentInstallCommandService(TunnelLifecycleManager tunnelLifecycleManager) {
        this.tunnelLifecycleManager = tunnelLifecycleManager;
    }

    public AgentInstallPreview preview() {
        TunnelStatus status = tunnelLifecycleManager.status();
        String serverUrl = status.publicUrl() != null && !status.publicUrl().isBlank()
                ? status.publicUrl()
                : "http://localhost:8080";
        String windows = String.join(" && ",
                "set AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token",
                "set AIOPS_SERVER_URL=" + serverUrl,
                "cd aiops-agent",
                "go run ./cmd -c config.yaml"
        );
        String linux = String.join(" && ",
                "export AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token",
                "export AIOPS_SERVER_URL='" + serverUrl + "'",
                "cd aiops-agent",
                "go run ./cmd -c config.yaml"
        );
        String note = status.publicUrl() == null || status.publicUrl().isBlank()
                ? "Tunnel publicUrl not detected yet. Replace localhost with a reachable backend address before using on a remote server."
                : "Use the detected publicUrl for remote agents. localhost only works on the same machine as backend.";
        return new AgentInstallPreview(serverUrl, "aiops-mvp-seed-demo-token", windows, linux, note);
    }
}
