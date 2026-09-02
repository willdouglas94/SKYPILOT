package com.skypilot.backend.aviation.mapper;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.domain.Airport;
import com.skypilot.backend.domain.Route;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class FlightAwareMapper {

    public List<Route> toRoutes(List<FlightAwareClient.FlightAwareRoute> flightAwareRoutes) {
        if (flightAwareRoutes == null || flightAwareRoutes.isEmpty()) {
            return List.of();
        }

        return flightAwareRoutes.stream()
                .filter(route -> route != null)
                .map(this::toRoute)
                .filter(route -> route != null && route.getOrigin() != null && route.getDestination() != null)
                .toList();
    }

    public Route toRoute(FlightAwareClient.FlightAwareRoute route) {
        if (route == null) {
            return null;
        }

        String normalizedOrigin = normalizeCode(route.originCode());
        String normalizedDestination = normalizeCode(route.destinationCode());
        String source = normalizeText(route.source(), "FLIGHTAWARE");
        String externalId = normalizeText(route.id(), "FA-" + normalizedOrigin + "-" + normalizedDestination);
        Instant now = Instant.now();

        Airport origin = new Airport(
                normalizedOrigin,
                "UNKNOWN",
                "UNKNOWN",
                source,
                externalId,
                now,
                now
        );

        Airport destination = new Airport(
                normalizedDestination,
                "UNKNOWN",
                "UNKNOWN",
                source,
                externalId,
                now,
                now
        );

        return new Route(
                externalId,
                origin,
                destination,
                normalizeDistance(route.distanceKm()),
                normalizeDuration(route.durationMinutes()),
                source,
                externalId,
                now,
                now
        );
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private int normalizeDistance(int distanceKm) {
        return distanceKm > 0 ? distanceKm : 1500;
    }

    private int normalizeDuration(int durationMinutes) {
        return durationMinutes > 0 ? durationMinutes : 120;
    }
}
