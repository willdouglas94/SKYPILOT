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

@Entity
@Table(name = "aircraft")
public class AircraftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String registration;

    @ManyToOne
    @JoinColumn(name = "aircraft_type_id", nullable = false)
    private AircraftTypeEntity aircraftType;

    @ManyToOne
    @JoinColumn(name = "airline_id", nullable = false)
    private AirlineEntity airline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType dataSource = DataSourceType.USER_CREATED;

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE";

    protected AircraftEntity() {
    }

    public AircraftEntity(String registration, AircraftTypeEntity aircraftType, AirlineEntity airline,
                         DataSourceType dataSource, String status) {
        this.registration = registration == null || registration.isBlank() ? "UNKNOWN" : registration.trim().toUpperCase();
        this.aircraftType = aircraftType;
        this.airline = airline;
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.status = status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase();
    }

    public String getId() {
        return id;
    }

    public String getRegistration() {
        return registration;
    }

    public AircraftTypeEntity getAircraftType() {
        return aircraftType;
    }

    public AirlineEntity getAirline() {
        return airline;
    }

    public DataSourceType getDataSource() {
        return dataSource;
    }

    public String getStatus() {
        return status;
    }
}
