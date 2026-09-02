package com.skypilot.backend.repository;

import com.skypilot.backend.domain.DataSourceType;
import com.skypilot.backend.domain.FlightScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightScheduleEntity, String> {
    List<FlightScheduleEntity> findByDataSource(DataSourceType dataSource);
    List<FlightScheduleEntity> findByRoute_Id(String routeId);
    List<FlightScheduleEntity> findByFlight_Id(String flightId);
}
