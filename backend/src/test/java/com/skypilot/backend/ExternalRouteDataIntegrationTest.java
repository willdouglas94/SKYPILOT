package com.skypilot.backend;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.aviation.mapper.FlightAwareMapper;
import com.skypilot.backend.domain.Route;
import com.skypilot.backend.service.ExternalFlightDataService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalRouteDataIntegrationTest {

    @Test
    void shouldLoadExternalRoutesFromConfiguredProvider() {
        ExternalFlightDataService service = new ExternalFlightDataService(
                new FlightAwareClient("", ""),
                new FlightAwareMapper()
        );

        List<Route> routes = service.fetchRoutes();

        assertThat(routes).isNotEmpty();
        assertThat(routes).anySatisfy(route -> {
            assertThat(route.getOrigin().getCode()).isNotBlank();
            assertThat(route.getDestination().getCode()).isNotBlank();
            assertThat(route.getDistanceKm()).isPositive();
        });
    }
}
