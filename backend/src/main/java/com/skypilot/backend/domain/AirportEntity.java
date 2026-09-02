package com.skypilot.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "airports")
public class AirportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(length = 255)
    private String city;

    @Column(length = 255)
    private String country;

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

    protected AirportEntity() {
    }

    public AirportEntity(String code, String city, String country, DataSourceType dataSource) {
        this.code = code == null || code.isBlank() ? "UNKNOWN" : code.trim().toUpperCase();
        this.city = city == null || city.isBlank() ? "UNKNOWN" : city.trim();
        this.country = country == null || country.isBlank() ? "UNKNOWN" : country.trim();
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.lastSyncedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
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
