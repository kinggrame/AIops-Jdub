package com.aiops.llm.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String baseUrl = "http://localhost:11434";

    public OllamaClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String chat(String model, String prompt) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("prompt", prompt);
            request.put("stream", false);

            String url = baseUrl + "/api/generate";
            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode json = objectMapper.readTree(response);
            return json.has("response") ? json.get("response").asText() : response;
        } catch (Exception e) {
            throw new RuntimeException("Ollama chat failed: " + e.getMessage(), e);
        }
    }

    public List<Float> embed(String model, String text) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("input", text);

            String url = baseUrl + "/api/embeddings";
            String response = restTemplate.postForObject(url, request, String.class);

            JsonNode json = objectMapper.readTree(response);
            if (json.has("embedding")) {
                List<Float> result = new ArrayList<>();
                json.get("embedding").forEach(node -> result.add((float) node.asDouble()));
                return result;
            }
            throw new RuntimeException("No embedding returned");
        } catch (Exception e) {
            throw new RuntimeException("Ollama embedding failed: " + e.getMessage(), e);
        }
    }

    public List<String> listModels() {
        try {
            String url = baseUrl + "/api/tags";
            String response = restTemplate.getForObject(url, String.class);

            List<String> models = new ArrayList<>();
            JsonNode json = objectMapper.readTree(response);
            if (json.has("models")) {
                json.get("models").forEach(model -> {
                    models.add(model.get("name").asText());
                });
            }
            return models;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list models: " + e.getMessage(), e);
        }
    }

    public boolean testConnection() {
        try {
            listModels();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class OllamaModel {
        private String name;
        private String size;
        private String modifiedAt;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public String getModifiedAt() { return modifiedAt; }
        public void setModifiedAt(String modifiedAt) { this.modifiedAt = modifiedAt; }
    }
}
