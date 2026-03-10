package com.aiops.llm.repository;

import com.aiops.llm.model.LlmProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LlmProviderRepository extends JpaRepository<LlmProvider, String> {
    List<LlmProvider> findByEnabledTrue();
    List<LlmProvider> findByModelType(LlmProvider.ModelType modelType);
    List<LlmProvider> findByEnabledTrueAndModelTypeOrderByPriorityAsc(LlmProvider.ModelType modelType);
}
