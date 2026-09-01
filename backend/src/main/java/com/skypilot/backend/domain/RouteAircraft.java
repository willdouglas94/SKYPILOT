package com.skypilot.backend.domain;

public class RouteAircraft {
    private final String routeId;
    private final String aircraftTypeId;
    private final boolean compatible;

    public RouteAircraft(String routeId, String aircraftTypeId, boolean compatible) {
        this.routeId = routeId == null || routeId.isBlank() ? java.util.UUID.randomUUID().toString() : routeId;
        this.aircraftTypeId = aircraftTypeId == null || aircraftTypeId.isBlank() ? "AT-DEFAULT" : aircraftTypeId;
        this.compatible = compatible;
    }

    public String getRouteId() { return routeId; }
    public String getAircraftTypeId() { return aircraftTypeId; }
    public boolean isCompatible() { return compatible; }
}
