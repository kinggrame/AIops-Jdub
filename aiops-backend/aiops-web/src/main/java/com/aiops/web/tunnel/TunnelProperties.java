package com.aiops.web.tunnel;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiops.tunnel")
public class TunnelProperties {

    private boolean enabled;
    private String command = "cloudflared";
    private String arguments = "tunnel --url http://127.0.0.1:8080";
    private boolean allowNonLocalUrl;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public boolean isAllowNonLocalUrl() {
        return allowNonLocalUrl;
    }

    public void setAllowNonLocalUrl(boolean allowNonLocalUrl) {
        this.allowNonLocalUrl = allowNonLocalUrl;
    }
}
