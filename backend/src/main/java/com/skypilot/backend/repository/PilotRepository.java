package com.skypilot.backend.repository;

import com.skypilot.backend.domain.PilotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilotRepository extends JpaRepository<PilotEntity, String> {
}
