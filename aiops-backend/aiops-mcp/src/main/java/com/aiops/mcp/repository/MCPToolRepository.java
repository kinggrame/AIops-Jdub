package com.aiops.mcp.repository;

import com.aiops.mcp.model.MCPTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MCPToolRepository extends JpaRepository<MCPTool, String> {
    List<MCPTool> findByServerId(String serverId);
    List<MCPTool> findByEnabledTrue();
}
