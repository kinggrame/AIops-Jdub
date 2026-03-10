package com.aiops.connection.dto;

public class PairingRequest {

    private String pairingToken;
    private String agentId;
    private String serverUrl;
    private String hostname;
    private String endpoint;

    public PairingRequest() {}

    // Getters and Setters

    public String getPairingToken() {
        return pairingToken;
    }

    public void setPairingToken(String pairingToken) {
        this.pairingToken = pairingToken;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
