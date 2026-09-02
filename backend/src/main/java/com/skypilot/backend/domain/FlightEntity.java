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
@Table(name = "flights")
public class FlightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @Column(nullable = false, length = 50)
    private String flightNumber;

    @ManyToOne
    @JoinColumn(name = "aircraft_id")
    private AircraftEntity aircraft;

    @Column(nullable = false)
    private Instant departureAt;

    @Column(nullable = false)
    private Instant arrivalAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType dataSource = DataSourceType.USER_CREATED;

    @Column(length = 255)
    private String externalId;

    @Column
    private Instant sourceUpdatedAt;

    @Column(nullable = false)
    private Instant lastSyncedAt = Instant.now();

    protected FlightEntity() {
    }

    public FlightEntity(RouteEntity route, String flightNumber, AircraftEntity aircraft,
                       Instant departureAt, Instant arrivalAt, DataSourceType dataSource) {
        this.route = route;
        this.flightNumber = flightNumber == null || flightNumber.isBlank() ? "UNKNOWN" : flightNumber.trim().toUpperCase();
        this.aircraft = aircraft;
        this.departureAt = departureAt == null ? Instant.now() : departureAt;
        this.arrivalAt = arrivalAt == null ? this.departureAt.plusSeconds(3600) : arrivalAt;
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.lastSyncedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public RouteEntity getRoute() {
        return route;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public AircraftEntity getAircraft() {
        return aircraft;
    }

    public Instant getDepartureAt() {
        return departureAt;
    }

    public Instant getArrivalAt() {
        return arrivalAt;
    }

    public DataSourceType getDataSource() {
        return dataSource;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Instant getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }

    public void setSourceUpdatedAt(Instant sourceUpdatedAt) {
        this.sourceUpdatedAt = sourceUpdatedAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
