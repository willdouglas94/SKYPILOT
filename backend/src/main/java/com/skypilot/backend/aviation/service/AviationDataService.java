package com.skypilot.backend.aviation.service;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.aviation.mapper.FlightAwareMapper;
import com.skypilot.backend.domain.Route;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AviationDataService {

    private final FlightAwareClient flightAwareClient;
    private final FlightAwareMapper flightAwareMapper;

    public AviationDataService(FlightAwareClient flightAwareClient, FlightAwareMapper flightAwareMapper) {
        this.flightAwareClient = flightAwareClient;
        this.flightAwareMapper = flightAwareMapper;
    }

    public List<Route> loadRoutes() {
        List<FlightAwareClient.FlightAwareRoute> fetched = flightAwareClient.fetchRoutes();
        List<Route> routes = flightAwareMapper.toRoutes(fetched);
        if (routes != null && !routes.isEmpty()) {
            return routes;
        }
        return List.of();
    }
}
