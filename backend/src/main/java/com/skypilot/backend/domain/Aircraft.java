package com.skypilot.backend.domain;

public class Aircraft {
    private final String id;
    private final String registration;
    private final AircraftType aircraftType;
    private final Airline airline;
    private final String status;

    public Aircraft(String id, String registration, AircraftType aircraftType, Airline airline, String status) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.registration = registration == null || registration.isBlank() ? "PR-SKY" : registration.trim().toUpperCase();
        this.aircraftType = aircraftType == null ? new AircraftType("default", "Airbus", "A320", "NARROWBODY", 5000, 180) : aircraftType;
        this.airline = airline == null ? new Airline("default-airline", "SkyPilot", "SP", "SKP", "Brazil", "GRU") : airline;
        this.status = status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase();
    }

    public String getId() { return id; }
    public String getRegistration() { return registration; }
    public AircraftType getAircraftType() { return aircraftType; }
    public Airline getAirline() { return airline; }
    public String getStatus() { return status; }
}
