package com.aiops.llm.service;

import com.aiops.llm.client.OllamaClient;
import com.aiops.llm.model.LlmProvider;
import com.aiops.llm.repository.LlmProviderRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LlmService {
    private final LlmProviderRepository providerRepository;
    private final OllamaClient ollamaClient;

    public LlmService(LlmProviderRepository providerRepository, OllamaClient ollamaClient) {
        this.providerRepository = providerRepository;
        this.ollamaClient = ollamaClient;
    }

    public List<LlmProvider> findAllProviders() { return providerRepository.findAll(); }
    public List<LlmProvider> findEnabledProviders() { return providerRepository.findByEnabledTrue(); }
    public LlmProvider findProviderById(String id) { return providerRepository.findById(id).orElse(null); }
    public LlmProvider saveProvider(LlmProvider provider) {
        provider.setUpdatedAt(java.time.LocalDateTime.now());
        return providerRepository.save(provider);
    }
    public void deleteProvider(String id) { providerRepository.deleteById(id); }

    public List<LlmProvider> findChatProviders() {
        return providerRepository.findByEnabledTrueAndModelTypeOrderByPriorityAsc(LlmProvider.ModelType.CHAT);
    }

    public List<LlmProvider> findEmbeddingProviders() {
        return providerRepository.findByEnabledTrueAndModelTypeOrderByPriorityAsc(LlmProvider.ModelType.EMBEDDING);
    }

    public LlmProvider getDefaultChatProvider() {
        List<LlmProvider> providers = findChatProviders();
        return providers.isEmpty() ? null : providers.get(0);
    }

    public LlmProvider getDefaultEmbeddingProvider() {
        List<LlmProvider> providers = findEmbeddingProviders();
        return providers.isEmpty() ? null : providers.get(0);
    }

    public Map<String, Object> chat(String message, String providerId) {
        LlmProvider provider = providerId != null ? findProviderById(providerId) : getDefaultChatProvider();
        if (provider == null) {
            throw new RuntimeException("No available chat provider");
        }

        String response;
        if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
            ollamaClient.setBaseUrl(provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434");
            response = ollamaClient.chat(provider.getDefaultModel(), message);
        } else {
            response = "Provider type " + provider.getType() + " not implemented yet";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", response);
        result.put("model", provider.getDefaultModel());
        result.put("provider", provider.getName());
        result.put("usage", Map.of("input_tokens", message.length() / 4, "output_tokens", response.length() / 4));
        return result;
    }

    public Map<String, Object> embed(String text, String providerId) {
        LlmProvider provider = providerId != null ? findProviderById(providerId) : getDefaultEmbeddingProvider();
        if (provider == null) {
            throw new RuntimeException("No available embedding provider");
        }

        List<Double> embedding;
        if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
            ollamaClient.setBaseUrl(provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434");
            List<Float> floats = ollamaClient.embed(provider.getDefaultModel(), text);
            embedding = new ArrayList<>();
            for (Float f : floats) {
                embedding.add(f.doubleValue());
            }
        } else {
            embedding = new ArrayList<>();
            for (int i = 0; i < provider.getDimensions(); i++) {
                embedding.add(Math.random());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("embedding", embedding);
        result.put("model", provider.getDefaultModel());
        result.put("provider", provider.getName());
        result.put("dimensions", provider.getDimensions());
        return result;
    }

    public boolean testConnection(String providerId) {
        LlmProvider provider = findProviderById(providerId);
        if (provider == null) {
            return false;
        }

        if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
            ollamaClient.setBaseUrl(provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434");
            return ollamaClient.testConnection();
        } else if (provider.getType() == LlmProvider.ProviderType.OPENAI) {
            return provider.getApiKey() != null && !provider.getApiKey().isEmpty();
        }

        return false;
    }

    public List<String> listModels(String providerId) {
        LlmProvider provider = findProviderById(providerId);
        if (provider == null) {
            return Collections.emptyList();
        }

        if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
            ollamaClient.setBaseUrl(provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434");
            return ollamaClient.listModels();
        }

        return Collections.emptyList();
    }

    public void initializeDefaultProviders() {
        if (providerRepository.count() == 0) {
            createDefaultProvider("openai-chat", "OpenAI Chat", LlmProvider.ProviderType.OPENAI, 
                LlmProvider.ModelType.CHAT, "gpt-4", 0.7, 2000);
            createDefaultProvider("ollama-chat", "Ollama Local", LlmProvider.ProviderType.OLLAMA, 
                LlmProvider.ModelType.CHAT, "llama3", 0.7, 2000);
        }
    }

    private void createDefaultProvider(String id, String name, LlmProvider.ProviderType type, 
            LlmProvider.ModelType modelType, String model, Double temp, Integer tokens) {
        LlmProvider provider = new LlmProvider();
        provider.setId(id);
        provider.setName(name);
        provider.setType(type);
        provider.setModelType(modelType);
        provider.setDefaultModel(model);
        provider.setTemperature(temp);
        provider.setMaxTokens(tokens);
        provider.setEnabled(true);
        provider.setPriority(1);
        if (type == LlmProvider.ProviderType.OLLAMA) {
            provider.setEndpoint("http://localhost:11434");
        }
        providerRepository.save(provider);
    }
}
