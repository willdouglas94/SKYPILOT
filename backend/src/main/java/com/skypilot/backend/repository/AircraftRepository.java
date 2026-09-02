package com.skypilot.backend.repository;

import com.skypilot.backend.domain.AircraftEntity;
import com.skypilot.backend.domain.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<AircraftEntity, String> {
    Optional<AircraftEntity> findByRegistration(String registration);
    List<AircraftEntity> findByDataSource(DataSourceType dataSource);
    List<AircraftEntity> findByAirline_Id(String airlineId);
}
