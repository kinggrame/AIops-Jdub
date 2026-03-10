package com.aiops.connection.service;

import com.aiops.connection.dto.PairingRequest;
import com.aiops.connection.dto.PairingResponse;
import com.aiops.connection.model.AgentConnection;
import com.aiops.connection.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PairingService {

    @Autowired
    private ConnectionRepository repository;

    @Value("${aiops.server.url:http://localhost:8080}")
    private String serverUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public PairingResponse processPairingRequest(PairingRequest request) {
        // 1. 查找匹配的连接配置
        Optional<AgentConnection> connOpt = repository.findByPairingToken(request.getPairingToken());
        
        if (connOpt.isEmpty()) {
            return PairingResponse.rejected("无效的配对Token");
        }
        
        AgentConnection conn = connOpt.get();
        
        // 2. 检查状态
        if (conn.getStatus() == AgentConnection.ConnectionStatus.CONNECTED) {
            return PairingResponse.rejected("该Agent已经配对过");
        }
        
        if (conn.getStatus() == AgentConnection.ConnectionStatus.REJECTED) {
            return PairingResponse.rejected("该配对请求已被拒绝");
        }
        
        // 3. 生成authorization token
        String authToken = generateAuthToken();
        
        // 4. 保存到连接配置
        conn.setAuthorizationToken(authToken);
        conn.setTokenIssuedAt(LocalDateTime.now());
        conn.setTokenExpiresAt(LocalDateTime.now().plusYears(1));
        conn.setStatus(AgentConnection.ConnectionStatus.CONNECTED);
        conn.setPairingAt(LocalDateTime.now());
        conn.setLastConnectedAt(LocalDateTime.now());
        conn.setUpdatedAt(LocalDateTime.now());
        
        repository.save(conn);
        
        // 5. 返回配对成功响应
        return PairingResponse.approved(authToken, serverUrl, conn.getId());
    }

    public String initiatePairing(String endpoint) {
        try {
            // 调用Agent的配对接口
            String url = endpoint + "/api/pairing/initiate";
            
            // 这里应该调用Agent，但目前Agent端还没实现
            // 先返回配对URL供用户手动操作
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate pairing: " + e.getMessage());
        }
    }

    public boolean verifyPairingToken(String pairingToken) {
        return repository.existsByPairingToken(pairingToken);
    }

    private String generateAuthToken() {
        return "auth-" + UUID.randomUUID().toString().replace("-", "");
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
