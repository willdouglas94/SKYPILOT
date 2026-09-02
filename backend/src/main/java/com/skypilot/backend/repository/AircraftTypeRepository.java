package com.skypilot.backend.repository;

import com.skypilot.backend.domain.AircraftTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AircraftTypeRepository extends JpaRepository<AircraftTypeEntity, String> {
    Optional<AircraftTypeEntity> findByManufacturerAndModel(String manufacturer, String model);
}
