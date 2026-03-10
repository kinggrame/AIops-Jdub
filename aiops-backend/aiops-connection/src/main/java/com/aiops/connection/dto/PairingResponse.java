package com.aiops.connection.dto;

public class PairingResponse {

    private String status;
    private String message;
    private String authorizationToken;
    private String serverUrl;
    private String agentId;

    public PairingResponse() {}

    public PairingResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static PairingResponse approved(String authToken, String serverUrl, String agentId) {
        PairingResponse resp = new PairingResponse();
        resp.status = "APPROVED";
        resp.message = "配对成功";
        resp.authorizationToken = authToken;
        resp.serverUrl = serverUrl;
        resp.agentId = agentId;
        return resp;
    }

    public static PairingResponse rejected(String message) {
        return new PairingResponse("REJECTED", message);
    }

    public static PairingResponse pending(String message) {
        return new PairingResponse("PENDING", message);
    }

    // Getters and Setters

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
