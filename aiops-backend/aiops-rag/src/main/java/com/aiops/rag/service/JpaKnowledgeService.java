package com.aiops.rag.service;

import com.aiops.rag.entity.KnowledgeDocument;
import com.aiops.rag.entity.KnowledgeDocumentEntity;
import com.aiops.rag.repository.KnowledgeDocumentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Primary
public class JpaKnowledgeService implements KnowledgeService {

    private final KnowledgeDocumentRepository repository;

    public JpaKnowledgeService(KnowledgeDocumentRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void seedIfEmpty() {
        if (repository.count() > 0) {
            return;
        }
        saveSeed("CPU 高负载排查", "CPU 持续高于 90% 时优先检查高占用进程、最近发布和流量突增。", "incident", List.of("cpu", "load", "process", "restart"));
        saveSeed("Nginx 重启案例", "历史案例表明 nginx worker 异常时重启服务可快速恢复。", "playbook", List.of("nginx", "restart", "cpu"));
        saveSeed("内存告警处理", "内存超过 85% 时建议先抓取 top 和 GC 信息，再考虑扩容或重启。", "playbook", List.of("memory", "oom", "gc"));
    }

    @Override
    public List<KnowledgeDocument> search(String query, int topK) {
        return score(query).stream().limit(topK).toList();
    }

    private List<KnowledgeDocument> score(String query) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .map(this::toDomain)
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

    private void saveSeed(String title, String content, String category, List<String> keywords) {
        KnowledgeDocumentEntity entity = new KnowledgeDocumentEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTitle(title);
        entity.setContent(content);
        entity.setCategory(category);
        entity.setKeywords(keywords);
        repository.save(entity);
    }

    private KnowledgeDocument toDomain(KnowledgeDocumentEntity entity) {
        return new KnowledgeDocument(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCategory(),
                entity.getKeywords(),
                0
        );
    }
}
