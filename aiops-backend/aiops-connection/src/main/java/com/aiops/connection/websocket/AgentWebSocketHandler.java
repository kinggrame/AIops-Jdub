package com.aiops.connection.websocket;

import com.aiops.connection.config.AgentTokenUtils;
import com.aiops.connection.service.AgentRegistryService;
import com.aiops.connection.service.AgentSessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {

    private final AgentSessionService agentSessionService;
    private final AgentRegistryService agentRegistryService;

    public AgentWebSocketHandler(AgentSessionService agentSessionService,
                                 AgentRegistryService agentRegistryService) {
        this.agentSessionService = agentSessionService;
        this.agentRegistryService = agentRegistryService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String agentId = extractAgentId(session);
        String token = AgentTokenUtils.extractBearerToken(session.getHandshakeHeaders().getFirst("Authorization"));
        if (agentRegistryService.authenticate(agentId, token).isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized agent websocket"));
            return;
        }
        agentSessionService.register(agentId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // The MVP backend receives command results via REST, so WS messages are ignored here.
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        agentSessionService.unregister(extractAgentId(session), session.getId());
    }

    private String extractAgentId(WebSocketSession session) {
        String path = session.getUri() == null ? "" : session.getUri().getPath();
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
