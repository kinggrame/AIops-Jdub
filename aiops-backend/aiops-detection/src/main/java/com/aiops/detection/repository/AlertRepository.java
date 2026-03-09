package com.aiops.detection.repository;

import com.aiops.detection.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<AlertEntity, String> {
}
