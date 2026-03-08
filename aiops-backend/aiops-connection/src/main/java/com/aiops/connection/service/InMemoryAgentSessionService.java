package com.aiops.connection.service;

import com.aiops.common.exception.BusinessException;
import com.aiops.connection.model.CommandEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryAgentSessionService implements AgentSessionService {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public InMemoryAgentSessionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void register(String agentId, WebSocketSession session) {
        sessions.put(agentId, session);
    }

    @Override
    public void unregister(String agentId, String sessionId) {
        WebSocketSession session = sessions.get(agentId);
        if (session != null && session.getId().equals(sessionId)) {
            sessions.remove(agentId);
        }
    }

    @Override
    public boolean isConnected(String agentId) {
        WebSocketSession session = sessions.get(agentId);
        return session != null && session.isOpen();
    }

    @Override
    public void send(String agentId, CommandEnvelope envelope) {
        WebSocketSession session = sessions.get(agentId);
        if (session == null || !session.isOpen()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Agent websocket not connected: " + agentId);
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize command envelope");
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to send command to agent");
        }
    }
}
