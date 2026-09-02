package com.skypilot.backend.repository;

import com.skypilot.backend.domain.DataSourceType;
import com.skypilot.backend.domain.RouteAircraftEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteAircraftRepository extends JpaRepository<RouteAircraftEntity, String> {
    List<RouteAircraftEntity> findByDataSource(DataSourceType dataSource);
    List<RouteAircraftEntity> findByRoute_Id(String routeId);
    List<RouteAircraftEntity> findByAircraftType_Id(String aircraftTypeId);
}
