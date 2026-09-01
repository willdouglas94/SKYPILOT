package com.skypilot.backend.aviation.mapper;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.domain.Airport;
import com.skypilot.backend.domain.Route;
import org.springframework.stereotype.Component;

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
                .toList();
    }

    public Route toRoute(FlightAwareClient.FlightAwareRoute route) {
        if (route == null) {
            return null;
        }
        return new Route(
                route.id() != null ? route.id() : "FA-" + route.originCode() + "-" + route.destinationCode(),
                new Airport(route.originCode(), route.originCode(), "External"),
                new Airport(route.destinationCode(), route.destinationCode(), "External"),
                route.distanceKm(),
                route.durationMinutes()
        );
    }
}
