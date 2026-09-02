package com.skypilot.backend.repository;

import com.skypilot.backend.domain.AirportEntity;
import com.skypilot.backend.domain.DataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<AirportEntity, String> {
    Optional<AirportEntity> findByCode(String code);
    List<AirportEntity> findByDataSource(DataSourceType dataSource);
}
