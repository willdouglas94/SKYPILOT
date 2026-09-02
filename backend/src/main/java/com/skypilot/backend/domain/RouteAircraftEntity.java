package com.skypilot.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "route_aircraft")
public class RouteAircraftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @ManyToOne
    @JoinColumn(name = "aircraft_type_id", nullable = false)
    private AircraftTypeEntity aircraftType;

    @Column(nullable = false)
    private boolean compatible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType dataSource = DataSourceType.USER_CREATED;

    @Column(nullable = false)
    private Instant lastSyncedAt = Instant.now();

    protected RouteAircraftEntity() {
    }

    public RouteAircraftEntity(RouteEntity route, AircraftTypeEntity aircraftType, boolean compatible, DataSourceType dataSource) {
        this.route = route;
        this.aircraftType = aircraftType;
        this.compatible = compatible;
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.lastSyncedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public RouteEntity getRoute() {
        return route;
    }

    public AircraftTypeEntity getAircraftType() {
        return aircraftType;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public DataSourceType getDataSource() {
        return dataSource;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
