package com.skypilot.backend.repository;

import com.skypilot.backend.domain.DataSourceType;
import com.skypilot.backend.domain.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<RouteEntity, String> {
    Optional<RouteEntity> findByOrigin_CodeAndDestination_Code(String originCode, String destinationCode);
    List<RouteEntity> findByDataSource(DataSourceType dataSource);
}
