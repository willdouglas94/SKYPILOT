package com.skypilot.backend.domain;

import java.time.DayOfWeek;

public class FlightSchedule {
    private final String id;
    private final Route route;
    private final String flightNumber;
    private final DayOfWeek dayOfWeek;
    private final String departureTime;
    private final String arrivalTime;

    public FlightSchedule(String id, Route route, String flightNumber, DayOfWeek dayOfWeek, String departureTime, String arrivalTime) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.route = route == null ? new Route("route-default", new Airport("GRU", "São Paulo", "Brazil"), new Airport("REC", "Recife", "Brazil"), 2800, 180) : route;
        this.flightNumber = flightNumber == null || flightNumber.isBlank() ? "SK-100" : flightNumber.trim().toUpperCase();
        this.dayOfWeek = dayOfWeek == null ? DayOfWeek.MONDAY : dayOfWeek;
        this.departureTime = departureTime == null || departureTime.isBlank() ? "08:00" : departureTime.trim();
        this.arrivalTime = arrivalTime == null || arrivalTime.isBlank() ? "10:00" : arrivalTime.trim();
    }

    public String getId() { return id; }
    public Route getRoute() { return route; }
    public String getFlightNumber() { return flightNumber; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
}
