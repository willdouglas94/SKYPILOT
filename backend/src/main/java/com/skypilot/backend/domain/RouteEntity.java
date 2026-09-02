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
@Table(name = "routes")
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "origin_airport_id", nullable = false)
    private AirportEntity origin;

    @ManyToOne
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private AirportEntity destination;

    @Column(nullable = false)
    private int distanceKm;

    @Column(nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType dataSource = DataSourceType.USER_CREATED;

    @Column(length = 100)
    private String provider = "UNKNOWN";

    @Column(length = 1000)
    private String providerMetadata;

    @Column(length = 255)
    private String externalId;

    @Column
    private Instant sourceUpdatedAt;

    @Column(nullable = false)
    private Instant lastSyncedAt = Instant.now();

    protected RouteEntity() {
    }

    public RouteEntity(AirportEntity origin, AirportEntity destination, int distanceKm, int durationMinutes,
                      DataSourceType dataSource) {
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm > 0 ? distanceKm : 1500;
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 120;
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.lastSyncedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public AirportEntity getOrigin() {
        return origin;
    }

    public AirportEntity getDestination() {
        return destination;
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public DataSourceType getDataSource() {
        return dataSource;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider == null || provider.isBlank() ? "UNKNOWN" : provider.trim();
    }

    public String getProviderMetadata() {
        return providerMetadata;
    }

    public void setProviderMetadata(String providerMetadata) {
        this.providerMetadata = providerMetadata;
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
