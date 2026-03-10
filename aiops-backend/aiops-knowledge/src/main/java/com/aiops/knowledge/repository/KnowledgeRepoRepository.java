package com.aiops.knowledge.repository;

import com.aiops.knowledge.model.KnowledgeRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeRepoRepository extends JpaRepository<KnowledgeRepo, Long> {
    List<KnowledgeRepo> findByOwnerId(Long ownerId);
    List<KnowledgeRepo> findByVisibility(KnowledgeRepo.RepoVisibility visibility);
}
