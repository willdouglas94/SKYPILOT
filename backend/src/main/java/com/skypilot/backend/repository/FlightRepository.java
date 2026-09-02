package com.skypilot.backend.repository;

import com.skypilot.backend.domain.DataSourceType;
import com.skypilot.backend.domain.FlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<FlightEntity, String> {
    Optional<FlightEntity> findByFlightNumber(String flightNumber);
    List<FlightEntity> findByDataSource(DataSourceType dataSource);
    List<FlightEntity> findByRoute_Id(String routeId);
}
