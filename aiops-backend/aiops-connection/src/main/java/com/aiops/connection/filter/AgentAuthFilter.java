package com.aiops.connection.filter;

import com.aiops.connection.model.AgentConnection;
import com.aiops.connection.service.ConnectionService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class AgentAuthFilter implements Filter {

    @Autowired
    private ConnectionService connectionService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        
        String uri = httpReq.getRequestURI();
        String method = httpReq.getMethod();
        
        // 允许的路径
        if (isAllowedPath(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 获取Token
        String token = extractToken(httpReq);
        
        if (token == null) {
            sendUnauthorized(httpRes, "Missing token");
            return;
        }
        
        // 验证Token
        AgentConnection conn = connectionService.findByAuthorizationToken(token).orElse(null);
        
        if (conn == null) {
            sendUnauthorized(httpRes, "Invalid token");
            return;
        }
        
        // 验证状态
        if (conn.getStatus() != AgentConnection.ConnectionStatus.CONNECTED) {
            httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
            sendError(httpRes, "Connection not active");
            return;
        }
        
        // 验证通过，放行
        httpReq.setAttribute("agentConnection", conn);
        chain.doFilter(request, response);
    }

    private boolean isAllowedPath(String uri) {
        // 配对接口
        if (uri.startsWith("/api/v1/pairing")) {
            return true;
        }
        
        // Agent端点
        if (uri.startsWith("/api/agent/")) {
            return true;
        }
        
        // 健康检查
        if (uri.equals("/api/health") || uri.equals("/actuator/health")) {
            return true;
        }
        
        // WebSocket
        if (uri.startsWith("/ws/")) {
            return true;
        }
        
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        // 从Header获取
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 从Query参数获取
        String queryToken = request.getParameter("token");
        if (queryToken != null) {
            return queryToken;
        }
        
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
