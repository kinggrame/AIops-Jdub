package com.aiops.tool.repository;

import com.aiops.tool.model.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, String> {
    List<Tool> findByEnabledTrue();
    List<Tool> findByType(Tool.ToolType type);
}
