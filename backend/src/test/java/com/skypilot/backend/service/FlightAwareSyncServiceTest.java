package com.skypilot.backend.service;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.domain.AirportEntity;
import com.skypilot.backend.domain.DataSourceType;
import com.skypilot.backend.domain.RouteEntity;
import com.skypilot.backend.repository.AirportRepository;
import com.skypilot.backend.repository.RouteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlightAwareSyncServiceTest {

    @Test
    void shouldPersistRealAirportRoutesFromFlightAware() {
        FlightAwareClient flightAwareClient = mock(FlightAwareClient.class);
        AirportRepository airportRepository = mock(AirportRepository.class);
        RouteRepository routeRepository = mock(RouteRepository.class);

        when(flightAwareClient.fetchRoutesForAirport("GRU")).thenReturn(List.of(
                new FlightAwareClient.FlightAwareRoute(
                        "FA-GRU-REC",
                        "GRU",
                        "REC",
                        2800,
                        180,
                        "A320",
                        "FLIGHTAWARE"
                )
        ));

        when(airportRepository.findByCode("GRU")).thenReturn(Optional.empty());
        when(airportRepository.findByCode("REC")).thenReturn(Optional.empty());
        when(airportRepository.save(any(AirportEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(routeRepository.findByOrigin_CodeAndDestination_Code("GRU", "REC")).thenReturn(Optional.empty());
        when(routeRepository.save(any(RouteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightAwareSyncService service = new FlightAwareSyncService(flightAwareClient, airportRepository, routeRepository);

        List<RouteEntity> result = service.syncRoutesForAirport("GRU");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DataSourceType.REAL, result.get(0).getDataSource());

        ArgumentCaptor<AirportEntity> airportCaptor = ArgumentCaptor.forClass(AirportEntity.class);
        verify(airportRepository, org.mockito.Mockito.times(2)).save(airportCaptor.capture());
        assertEquals(List.of("GRU", "REC"), airportCaptor.getAllValues().stream().map(AirportEntity::getCode).sorted().toList());
    }

    @Test
    void shouldSyncMultipleAirportsAndDeduplicateRoutesFromTheSameProvider() {
        FlightAwareClient flightAwareClient = mock(FlightAwareClient.class);
        AirportRepository airportRepository = mock(AirportRepository.class);
        RouteRepository routeRepository = mock(RouteRepository.class);

        when(flightAwareClient.fetchRoutesForAirport("GRU")).thenReturn(List.of(
                new FlightAwareClient.FlightAwareRoute("FA-GRU-REC", "GRU", "REC", 2800, 180, "A320", "FLIGHTAWARE"),
                new FlightAwareClient.FlightAwareRoute("FA-GRU-REC", "GRU", "REC", 2800, 180, "A320", "FLIGHTAWARE")
        ));
        when(flightAwareClient.fetchRoutesForAirport("GIG")).thenReturn(List.of(
                new FlightAwareClient.FlightAwareRoute("FA-GIG-CWB", "GIG", "CWB", 1600, 120, "B737", "FLIGHTAWARE")
        ));

        when(airportRepository.findByCode("GRU")).thenReturn(Optional.empty());
        when(airportRepository.findByCode("REC")).thenReturn(Optional.empty());
        when(airportRepository.findByCode("GIG")).thenReturn(Optional.empty());
        when(airportRepository.findByCode("CWB")).thenReturn(Optional.empty());
        when(airportRepository.save(any(AirportEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(routeRepository.findByOrigin_CodeAndDestination_Code("GRU", "REC")).thenReturn(Optional.empty());
        when(routeRepository.findByOrigin_CodeAndDestination_Code("GIG", "CWB")).thenReturn(Optional.empty());
        when(routeRepository.save(any(RouteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightAwareSyncService service = new FlightAwareSyncService(flightAwareClient, airportRepository, routeRepository);

        List<RouteEntity> result = service.syncRoutesForAirports(List.of("GRU", "GIG"));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("FLIGHTAWARE", result.get(0).getProvider());
        assertEquals("FLIGHTAWARE", result.get(1).getProvider());
    }
}
