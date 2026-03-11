package com.aiops.config.repository;

import com.aiops.config.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    List<SystemConfig> findByCategory(String category);
    List<SystemConfig> findByEditableTrue();
}
