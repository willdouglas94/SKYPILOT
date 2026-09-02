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
@Table(name = "airlines")
public class AirlineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 10)
    private String iata;

    @Column(length = 10)
    private String icao;

    @Column(length = 255)
    private String country;

    @Column(length = 255)
    private String baseCity;

    @Column(length = 10)
    private String mainAirportCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType dataSource = DataSourceType.USER_CREATED;

    @Column(length = 255)
    private String externalId;

    @Column
    private Instant sourceUpdatedAt;

    @Column(nullable = false)
    private Instant lastSyncedAt = Instant.now();

    protected AirlineEntity() {
    }

    public AirlineEntity(String name, String iata, String icao, String country, String baseCity, String mainAirportCode,
                        DataSourceType dataSource) {
        this.name = name == null || name.isBlank() ? "UNKNOWN" : name.trim();
        this.iata = iata == null || iata.isBlank() ? "XXX" : iata.trim().toUpperCase();
        this.icao = icao == null || icao.isBlank() ? "XXXX" : icao.trim().toUpperCase();
        this.country = country == null || country.isBlank() ? "UNKNOWN" : country.trim();
        this.baseCity = baseCity == null || baseCity.isBlank() ? "UNKNOWN" : baseCity.trim();
        this.mainAirportCode = mainAirportCode == null || mainAirportCode.isBlank() ? "UNKNOWN" : mainAirportCode.trim().toUpperCase();
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.lastSyncedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIata() {
        return iata;
    }

    public String getIcao() {
        return icao;
    }

    public String getCountry() {
        return country;
    }

    public String getBaseCity() {
        return baseCity;
    }

    public String getMainAirportCode() {
        return mainAirportCode;
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
