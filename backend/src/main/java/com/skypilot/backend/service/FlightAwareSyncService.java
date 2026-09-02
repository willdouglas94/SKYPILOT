package com.skypilot.backend.service;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.domain.AirportEntity;
import com.skypilot.backend.domain.DataSourceType;
import com.skypilot.backend.domain.RouteEntity;
import com.skypilot.backend.repository.AirportRepository;
import com.skypilot.backend.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FlightAwareSyncService {

    private static final Logger log = LoggerFactory.getLogger(FlightAwareSyncService.class);

    private final FlightAwareClient flightAwareClient;
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public FlightAwareSyncService(
            FlightAwareClient flightAwareClient,
            AirportRepository airportRepository,
            RouteRepository routeRepository
    ) {
        this.flightAwareClient = flightAwareClient;
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    @Transactional
    public List<RouteEntity> syncRoutesForAirport(String airportCode) {
        return syncRoutesForAirports(List.of(airportCode));
    }

    @Transactional
    public List<RouteEntity> syncRoutesForAirports(List<String> airportCodes) {
        if (airportCodes == null || airportCodes.isEmpty()) {
            return List.of();
        }

        Set<String> seenRoutes = new HashSet<>();
        List<RouteEntity> savedRoutes = new ArrayList<>();

        for (String airportCode : airportCodes) {
            if (airportCode == null || airportCode.isBlank()) {
                continue;
            }

            List<FlightAwareClient.FlightAwareRoute> fetchedRoutes = flightAwareClient.fetchRoutesForAirport(airportCode);
            if (fetchedRoutes == null || fetchedRoutes.isEmpty()) {
                continue;
            }

            for (FlightAwareClient.FlightAwareRoute flightAwareRoute : fetchedRoutes) {
                if (flightAwareRoute == null) {
                    continue;
                }

                String originCode = normalizeCode(flightAwareRoute.originCode());
                String destinationCode = normalizeCode(flightAwareRoute.destinationCode());
                if (originCode.equals("UNKNOWN") || destinationCode.equals("UNKNOWN")) {
                    continue;
                }

                String provider = normalizeProvider(flightAwareRoute.source());
                String externalId = resolveExternalId(flightAwareRoute);
                String dedupeKey = externalId != null ? externalId : originCode + "-" + destinationCode;
                if (!seenRoutes.add(dedupeKey)) {
                    continue;
                }

                AirportEntity origin = findOrCreateAirport(originCode, provider, flightAwareRoute);
                AirportEntity destination = findOrCreateAirport(destinationCode, provider, flightAwareRoute);

                RouteEntity routeEntity = routeRepository.findByOrigin_CodeAndDestination_Code(originCode, destinationCode)
                        .orElseGet(() -> new RouteEntity(origin, destination, flightAwareRoute.distanceKm(), flightAwareRoute.durationMinutes(), DataSourceType.REAL));

                routeEntity.setExternalId(externalId);
                routeEntity.setProvider(provider);
                routeEntity.setProviderMetadata(buildProviderMetadata(flightAwareRoute));
                routeEntity.setSourceUpdatedAt(Instant.now());
                routeEntity.setLastSyncedAt(Instant.now());

                savedRoutes.add(routeRepository.save(routeEntity));
            }
        }

        return savedRoutes;
    }

    private AirportEntity findOrCreateAirport(String code, String provider, FlightAwareClient.FlightAwareRoute flightAwareRoute) {
        String normalizedCode = normalizeCode(code);
        return airportRepository.findByCode(normalizedCode)
                .map(existing -> {
                    existing.setProvider(provider);
                    existing.setProviderMetadata(buildProviderMetadata(flightAwareRoute));
                    existing.setLastSyncedAt(Instant.now());
                    return airportRepository.save(existing);
                })
                .orElseGet(() -> {
                    AirportEntity airport = new AirportEntity(normalizedCode, "UNKNOWN", "UNKNOWN", DataSourceType.REAL);
                    airport.setProvider(provider);
                    airport.setProviderMetadata(buildProviderMetadata(flightAwareRoute));
                    airport.setExternalId(normalizedCode);
                    airport.setSourceUpdatedAt(Instant.now());
                    airport.setLastSyncedAt(Instant.now());
                    return airportRepository.save(airport);
                });
    }

    private String resolveExternalId(FlightAwareClient.FlightAwareRoute route) {
        if (route == null) {
            return null;
        }

        if (route.id() != null && !route.id().isBlank()) {
            return route.id().trim();
        }

        return "FA-" + normalizeCode(route.originCode()) + "-" + normalizeCode(route.destinationCode());
    }

    private String buildProviderMetadata(FlightAwareClient.FlightAwareRoute route) {
        if (route == null) {
            return "{}";
        }

        return String.format(
                "{\"provider\":\"%s\",\"aircraftType\":\"%s\",\"externalId\":\"%s\",\"distanceKm\":%d,\"durationMinutes\":%d}",
                normalizeProvider(route.source()),
                route.aircraftType() == null ? "UNKNOWN" : route.aircraftType().replace("\"", "\\\""),
                resolveExternalId(route),
                route.distanceKm(),
                route.durationMinutes()
        );
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return "FLIGHTAWARE";
        }
        return provider.trim().toUpperCase();
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.trim().toUpperCase();
    }
}
