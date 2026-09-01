package com.skypilot.backend.domain;

import java.time.LocalDate;

public class FlightOffer {
    private final String id;
    private final LocalDate date;
    private final Route route;
    private final Aircraft aircraft;
    private final String departureTime;
    private String status;
    private String assignedPilotId;
    private String completionNotes;
    private Integer completionScore;

    public FlightOffer(LocalDate date, Route route, Aircraft aircraft, String departureTime) {
        this(java.util.UUID.randomUUID().toString(), date, route, aircraft, departureTime, "PENDING");
    }

    public FlightOffer(String id, LocalDate date, Route route, Aircraft aircraft, String departureTime, String status) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.date = date == null ? LocalDate.now() : date;
        this.route = route == null ? new Route("route-default", new Airport("GRU", "São Paulo", "Brazil"), new Airport("REC", "Recife", "Brazil"), 2800, 180) : route;
        this.aircraft = aircraft == null ? new Aircraft("aircraft-default", "PR-SKY", new AircraftType("AT-1", "Airbus", "A320", "NARROWBODY", 5000, 180), new Airline("airline-default", "SkyPilot", "SP", "SKP", "Brazil", "GRU"), "ACTIVE") : aircraft;
        this.departureTime = departureTime == null || departureTime.isBlank() ? "09:40" : departureTime.trim();
        this.status = status == null || status.isBlank() ? "PENDING" : status.trim().toUpperCase();
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public Route getRoute() { return route; }
    public Aircraft getAircraft() { return aircraft; }
    public String getDepartureTime() { return departureTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status == null || status.isBlank() ? "PENDING" : status.trim().toUpperCase(); }
    public String getAssignedPilotId() { return assignedPilotId; }
    public void setAssignedPilotId(String assignedPilotId) { this.assignedPilotId = assignedPilotId == null || assignedPilotId.isBlank() ? null : assignedPilotId.trim(); }
    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    public Integer getCompletionScore() { return completionScore; }
    public void setCompletionScore(Integer completionScore) { this.completionScore = completionScore; }
}
