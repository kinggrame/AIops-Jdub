package com.aiops.knowledge.repository;

import com.aiops.knowledge.model.KnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {
    List<KnowledgeDoc> findByRepoId(Long repoId);
    void deleteByRepoId(Long repoId);
}
