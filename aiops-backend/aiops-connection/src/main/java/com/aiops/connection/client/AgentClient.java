package com.aiops.connection.client;

import com.aiops.connection.dto.PairingRequest;
import com.aiops.connection.dto.PairingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgentClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${aiops.server.url:http://localhost:8080}")
    private String serverUrl;

    public PairingResponse pair(String endpoint, PairingRequest request) {
        try {
            String url = endpoint + "/api/pairing/request";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<PairingRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PairingResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PairingResponse.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            return PairingResponse.rejected("配对请求失败: " + e.getMessage());
        }
    }

    public boolean testConnection(String endpoint) {
        try {
            String url = endpoint + "/api/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    public String executeCommand(String endpoint, String command, String token) {
        try {
            String url = endpoint + "/api/command/execute";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            String body = "{\"command\":\"" + command + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String getMetrics(String endpoint, String token) {
        try {
            String url = endpoint + "/api/metrics";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
