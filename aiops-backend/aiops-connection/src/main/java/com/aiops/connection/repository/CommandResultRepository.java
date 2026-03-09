package com.aiops.connection.repository;

import com.aiops.connection.model.CommandResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandResultRepository extends JpaRepository<CommandResultEntity, String> {
}
