package com.aiops.web.tunnel;

public record AgentInstallPreview(
        String serverUrl,
        String bootstrapTokenHint,
        String windowsCommand,
        String linuxCommand,
        String note
) {
}
