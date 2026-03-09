package com.aiops.search.repository;

import com.aiops.search.entity.LogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntryEntity, String> {
}
