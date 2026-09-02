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

import java.time.DayOfWeek;
import java.time.Instant;

@Entity
@Table(name = "flight_schedules")
public class FlightScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private FlightEntity flight;

    @Column(nullable = false)
    private String flightNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false, length = 10)
    private String departureTime;

    @Column(nullable = false, length = 10)
    private String arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType dataSource = DataSourceType.USER_CREATED;

    @Column
    private Instant lastSyncedAt = Instant.now();

    protected FlightScheduleEntity() {
    }

    public FlightScheduleEntity(RouteEntity route, String flightNumber, DayOfWeek dayOfWeek,
                               String departureTime, String arrivalTime, DataSourceType dataSource) {
        this.route = route;
        this.flightNumber = flightNumber == null || flightNumber.isBlank() ? "UNKNOWN" : flightNumber.trim().toUpperCase();
        this.dayOfWeek = dayOfWeek == null ? DayOfWeek.MONDAY : dayOfWeek;
        this.departureTime = departureTime == null || departureTime.isBlank() ? "09:00" : departureTime.trim();
        this.arrivalTime = arrivalTime == null || arrivalTime.isBlank() ? "11:00" : arrivalTime.trim();
        this.dataSource = dataSource == null ? DataSourceType.USER_CREATED : dataSource;
        this.lastSyncedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public RouteEntity getRoute() {
        return route;
    }

    public FlightEntity getFlight() {
        return flight;
    }

    public void setFlight(FlightEntity flight) {
        this.flight = flight;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
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
