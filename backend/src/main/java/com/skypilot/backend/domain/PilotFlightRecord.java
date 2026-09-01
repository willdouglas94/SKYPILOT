package com.skypilot.backend.domain;

import java.time.LocalDate;

public class PilotFlightRecord {
    private final String id;
    private final String pilotId;
    private final String routeCode;
    private final LocalDate flightDate;
    private final int score;
    private final String notes;

    public PilotFlightRecord(String id, String pilotId, String routeCode, LocalDate flightDate, int score, String notes) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.pilotId = pilotId == null || pilotId.isBlank() ? "unknown-pilot" : pilotId;
        this.routeCode = routeCode == null || routeCode.isBlank() ? "N/A" : routeCode;
        this.flightDate = flightDate == null ? LocalDate.now() : flightDate;
        this.score = Math.max(0, Math.min(100, score));
        this.notes = notes == null ? "" : notes;
    }

    public String getId() {
        return id;
    }

    public String getPilotId() {
        return pilotId;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public int getScore() {
        return score;
    }

    public String getNotes() {
        return notes;
    }
}
