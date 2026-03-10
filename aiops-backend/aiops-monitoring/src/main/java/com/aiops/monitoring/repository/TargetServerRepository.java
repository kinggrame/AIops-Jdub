package com.aiops.monitoring.repository;

import com.aiops.monitoring.model.TargetServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetServerRepository extends JpaRepository<TargetServer, Long> {

    List<TargetServer> findByStatus(TargetServer.ServerStatus status);

    List<TargetServer> findByGroupName(String groupName);

    List<TargetServer> findByAgentId(String agentId);
}
