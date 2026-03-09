package com.aiops.connection.repository;

import com.aiops.connection.model.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, String> {
    Optional<AgentEntity> findByHostname(String hostname);
}
