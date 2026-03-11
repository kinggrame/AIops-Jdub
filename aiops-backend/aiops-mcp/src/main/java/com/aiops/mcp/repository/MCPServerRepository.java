package com.aiops.mcp.repository;

import com.aiops.mcp.model.MCPServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MCPServerRepository extends JpaRepository<MCPServer, String> {
    List<MCPServer> findByEnabledTrue();
    List<MCPServer> findByType(MCPServer.MCPType type);
}
