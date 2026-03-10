package com.aiops.connection.repository;

import com.aiops.connection.model.PairingTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PairingTokenRepository extends JpaRepository<PairingTokenEntity, String> {
    Optional<PairingTokenEntity> findByTokenAndUsedFalse(String token);
}
