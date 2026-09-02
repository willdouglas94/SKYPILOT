package com.skypilot.backend.domain;

import java.time.Instant;

public class Route {
    private final String id;
    private final Airport origin;
    private final Airport destination;
    private final int distanceKm;
    private final int durationMinutes;
    private final String source;
    private final String externalId;
    private final Instant sourceUpdatedAt;
    private final Instant lastSyncedAt;

    public Route(String id, Airport origin, Airport destination, int distanceKm, int durationMinutes) {
        this(id, origin, destination, distanceKm, durationMinutes, "USER_CREATED", null, null, null);
    }

    public Route(String id, Airport origin, Airport destination, int distanceKm, int durationMinutes,
                 String source, String externalId, Instant sourceUpdatedAt, Instant lastSyncedAt) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.origin = origin == null ? new Airport("GRU", "São Paulo", "Brazil") : origin;
        this.destination = destination == null ? new Airport("REC", "Recife", "Brazil") : destination;
        this.distanceKm = distanceKm > 0 ? distanceKm : 1500;
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 120;
        this.source = source == null || source.isBlank() ? "USER_CREATED" : source.trim().toUpperCase();
        this.externalId = externalId == null || externalId.isBlank() ? null : externalId.trim();
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.lastSyncedAt = lastSyncedAt != null ? lastSyncedAt : Instant.now();
    }

    public String getId() { return id; }
    public Airport getOrigin() { return origin; }
    public Airport getDestination() { return destination; }
    public int getDistanceKm() { return distanceKm; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getSource() { return source; }
    public String getExternalId() { return externalId; }
    public Instant getSourceUpdatedAt() { return sourceUpdatedAt; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
}
