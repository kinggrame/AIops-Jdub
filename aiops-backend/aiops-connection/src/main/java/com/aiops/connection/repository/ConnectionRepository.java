package com.aiops.connection.repository;

import com.aiops.connection.model.AgentConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<AgentConnection, String> {

    List<AgentConnection> findAll();

    List<AgentConnection> findByStatus(AgentConnection.ConnectionStatus status);

    List<AgentConnection> findByGroupName(String groupName);

    Optional<AgentConnection> findByPairingToken(String pairingToken);

    Optional<AgentConnection> findByAuthorizationToken(String authorizationToken);

    @Query("SELECT c FROM AgentConnection c WHERE c.status = :status AND c.endpoint LIKE %:endpoint%")
    List<AgentConnection> findByStatusAndEndpointContaining(@Param("status") AgentConnection.ConnectionStatus status,
                                                           @Param("endpoint") String endpoint);

    boolean existsByPairingToken(String pairingToken);

    boolean existsByAuthorizationToken(String authorizationToken);
}
