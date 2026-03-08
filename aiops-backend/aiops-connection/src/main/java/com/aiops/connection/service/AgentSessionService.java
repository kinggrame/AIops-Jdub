package com.aiops.connection.service;

import com.aiops.connection.model.CommandEnvelope;
import org.springframework.web.socket.WebSocketSession;

public interface AgentSessionService {

    void register(String agentId, WebSocketSession session);

    void unregister(String agentId, String sessionId);

    boolean isConnected(String agentId);

    void send(String agentId, CommandEnvelope envelope);
}
