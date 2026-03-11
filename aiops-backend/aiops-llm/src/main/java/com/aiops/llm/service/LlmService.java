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

    public String chat(String message, String providerId) {
        LlmProvider provider = providerId != null ? findProviderById(providerId) : getDefaultChatProvider();
        if (provider == null) {
            return "No available chat provider. Please configure an LLM provider.";
        }

        try {
            if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
                String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434";
                ollamaClient.setBaseUrl(endpoint);
                return ollamaClient.chat(provider.getDefaultModel(), message);
            } else {
                return "Provider type " + provider.getType() + " not implemented. Use OLLAMA provider.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<Double> embed(String text, String providerId) {
        LlmProvider provider = providerId != null ? findProviderById(providerId) : getDefaultEmbeddingProvider();
        if (provider == null) {
            throw new RuntimeException("No available embedding provider");
        }

        try {
            if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
                String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434";
                ollamaClient.setBaseUrl(endpoint);
                List<Float> floats = ollamaClient.embed(provider.getDefaultModel(), text);
                List<Double> result = new ArrayList<>();
                for (Float f : floats) {
                    result.add(f.doubleValue());
                }
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("Embedding error: " + e.getMessage());
        }

        throw new RuntimeException("Provider type not supported");
    }

    public boolean testConnection(String providerId) {
        LlmProvider provider = findProviderById(providerId);
        if (provider == null) {
            return false;
        }

        if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
            String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434";
            ollamaClient.setBaseUrl(endpoint);
            return ollamaClient.testConnection();
        }

        return provider.getApiKey() != null && !provider.getApiKey().isEmpty();
    }

    public List<String> listModels(String providerId) {
        LlmProvider provider = findProviderById(providerId);
        if (provider == null) {
            return Collections.emptyList();
        }

        if (provider.getType() == LlmProvider.ProviderType.OLLAMA) {
            String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : "http://localhost:11434";
            ollamaClient.setBaseUrl(endpoint);
            return ollamaClient.listModels();
        }

        return Collections.emptyList();
    }

    public void initializeDefaultProviders() {
        if (providerRepository.count() == 0) {
            createDefaultProvider("ollama-local", "Ollama Local", LlmProvider.ProviderType.OLLAMA, 
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
