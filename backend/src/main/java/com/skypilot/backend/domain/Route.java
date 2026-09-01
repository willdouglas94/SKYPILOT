package com.skypilot.backend.domain;

public class Route {
    private final String id;
    private final Airport origin;
    private final Airport destination;
    private final int distanceKm;
    private final int durationMinutes;

    public Route(String id, Airport origin, Airport destination, int distanceKm, int durationMinutes) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.origin = origin == null ? new Airport("GRU", "São Paulo", "Brazil") : origin;
        this.destination = destination == null ? new Airport("REC", "Recife", "Brazil") : destination;
        this.distanceKm = distanceKm > 0 ? distanceKm : 1500;
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 120;
    }

    public String getId() { return id; }
    public Airport getOrigin() { return origin; }
    public Airport getDestination() { return destination; }
    public int getDistanceKm() { return distanceKm; }
    public int getDurationMinutes() { return durationMinutes; }
}
