package com.aiops.web.tunnel;

public record TunnelStatus(
        boolean enabled,
        boolean running,
        String command,
        String targetUrl,
        String publicUrl,
        String message
) {
}
