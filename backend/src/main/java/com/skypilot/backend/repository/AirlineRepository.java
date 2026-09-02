package com.skypilot.backend.repository;

import com.skypilot.backend.domain.AirlineEntity;
import com.skypilot.backend.domain.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirlineRepository extends JpaRepository<AirlineEntity, String> {
    Optional<AirlineEntity> findByIata(String iata);
    Optional<AirlineEntity> findByIcao(String icao);
    List<AirlineEntity> findByDataSource(DataSourceType dataSource);
}
