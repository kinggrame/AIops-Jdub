package com.aiops.rag.service;

import com.aiops.cache.service.CacheService;
import com.aiops.rag.entity.KnowledgeDocument;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class InMemoryKnowledgeService implements KnowledgeService {

    private final CacheService cacheService;
    private final List<KnowledgeDocument> documents;

    public InMemoryKnowledgeService(CacheService cacheService) {
        this.cacheService = cacheService;
        this.documents = List.of(
                new KnowledgeDocument(UUID.randomUUID().toString(), "CPU 高负载排查", "CPU 持续高于 90% 时优先检查高占用进程、最近发布和流量突增。", "incident", List.of("cpu", "load", "process", "restart"), 0),
                new KnowledgeDocument(UUID.randomUUID().toString(), "Nginx 重启案例", "历史案例表明 nginx worker 异常时重启服务可快速恢复。", "playbook", List.of("nginx", "restart", "cpu"), 0),
                new KnowledgeDocument(UUID.randomUUID().toString(), "内存告警处理", "内存超过 85% 时建议先抓取 top 和 GC 信息，再考虑扩容或重启。", "playbook", List.of("memory", "oom", "gc"), 0)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<KnowledgeDocument> search(String query, int topK) {
        String cacheKey = "knowledge:" + query + ":" + topK;
        Optional<List> cached = cacheService.get(cacheKey, List.class);
        return cached
                .map(value -> (List<KnowledgeDocument>) value)
                .orElseGet(() -> {
                    List<KnowledgeDocument> result = score(query).stream().limit(topK).toList();
                    cacheService.set(cacheKey, result, Duration.ofMinutes(10));
                    return result;
                });
    }

    private List<KnowledgeDocument> score(String query) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return documents.stream()
                .map(document -> new KnowledgeDocument(
                        document.id(),
                        document.title(),
                        document.content(),
                        document.category(),
                        document.keywords(),
                        document.keywords().stream().filter(normalized::contains).count() + similarityBonus(document, normalized)
                ))
                .sorted(Comparator.comparingDouble(KnowledgeDocument::score).reversed())
                .toList();
    }

    private double similarityBonus(KnowledgeDocument document, String query) {
        if (query.isBlank()) {
            return 0;
        }
        double bonus = 0;
        if (document.title().toLowerCase(Locale.ROOT).contains(query)) {
            bonus += 2;
        }
        if (document.content().toLowerCase(Locale.ROOT).contains(query)) {
            bonus += 1;
        }
        return bonus;
    }
}
