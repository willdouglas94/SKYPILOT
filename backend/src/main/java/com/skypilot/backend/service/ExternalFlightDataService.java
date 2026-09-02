package com.skypilot.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.skypilot.backend.aviation.mapper.FlightAwareMapper;
import com.skypilot.backend.domain.Airport;
import com.skypilot.backend.domain.Route;
import com.skypilot.backend.domain.RouteEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalFlightDataService {

    private static final Logger log = LoggerFactory.getLogger(ExternalFlightDataService.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FlightAwareClient flightAwareClient;
    private final FlightAwareMapper flightAwareMapper;
    private final FlightAwareSyncService flightAwareSyncService;

    @Value("${aviation.data.provider:demo}")
    private String provider;

    @Value("${aviation.data.base-url:}")
    private String baseUrl;

    @Value("${aviation.data.api-key:}")
    private String apiKey;

    public ExternalFlightDataService(FlightAwareClient flightAwareClient, FlightAwareMapper flightAwareMapper) {
        this(flightAwareClient, flightAwareMapper, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ExternalFlightDataService(FlightAwareClient flightAwareClient, FlightAwareMapper flightAwareMapper,
                                    FlightAwareSyncService flightAwareSyncService) {
        this.flightAwareClient = flightAwareClient;
        this.flightAwareMapper = flightAwareMapper;
        this.flightAwareSyncService = flightAwareSyncService;
    }

    public List<Route> fetchRoutes() {
        if (isRealProviderEnabled()) {
            try {
                List<FlightAwareClient.FlightAwareRoute> liveRoutes = flightAwareClient.fetchRoutes();
                List<Route> mappedRoutes = flightAwareMapper.toRoutes(liveRoutes);
                if (mappedRoutes != null && !mappedRoutes.isEmpty()) {
                    return mappedRoutes;
                }

                if (flightAwareSyncService != null) {
                    List<RouteEntity> syncedRoutes = flightAwareSyncService.syncRoutesForAirport("GRU");
                    if (syncedRoutes != null && !syncedRoutes.isEmpty()) {
                        return syncedRoutes.stream()
                                .map(routeEntity -> new Route(
                                        routeEntity.getExternalId() != null ? routeEntity.getExternalId() : routeEntity.getId(),
                                        new Airport(routeEntity.getOrigin().getCode(), routeEntity.getOrigin().getCity(), routeEntity.getOrigin().getCountry()),
                                        new Airport(routeEntity.getDestination().getCode(), routeEntity.getDestination().getCity(), routeEntity.getDestination().getCountry()),
                                        routeEntity.getDistanceKm(),
                                        routeEntity.getDurationMinutes(),
                                        routeEntity.getDataSource().name(),
                                        routeEntity.getExternalId(),
                                        routeEntity.getSourceUpdatedAt(),
                                        routeEntity.getLastSyncedAt()
                                ))
                                .toList();
                    }
                }
            } catch (Exception ex) {
                log.warn("External flight data provider failed. Falling back to demo route catalog. Cause: {}", ex.getMessage());
            }
        }

        return fallbackRoutes();
    }

    public List<Route> fetchBestCandidateRoutes(String originCode, String destinationCode) {
        List<Route> allRoutes = fetchRoutes();
        if (originCode == null || originCode.isBlank()) {
            return allRoutes;
        }

        String normalizedOrigin = originCode.trim().toUpperCase();
        return allRoutes.stream()
                .filter(route -> route.getOrigin().getCode().equals(normalizedOrigin))
                .filter(route -> destinationCode == null || destinationCode.isBlank() || route.getDestination().getCode().equalsIgnoreCase(destinationCode.trim()))
                .toList();
    }

    private boolean isRealProviderEnabled() {
        return "flightaware".equalsIgnoreCase(provider) || "aviation".equalsIgnoreCase(provider)
                || "live".equalsIgnoreCase(provider);
    }

    private List<Route> fetchLiveRoutes() throws Exception {
        String normalizedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (normalizedBaseUrl.isBlank()) {
            return List.of();
        }

        String url = normalizedBaseUrl.endsWith("/") ? normalizedBaseUrl + "routes" : normalizedBaseUrl + "/routes";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Provider returned HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode routesNode = root.has("routes") ? root.get("routes") : root;
        if (routesNode == null || !routesNode.isArray()) {
            return List.of();
        }

        List<Route> liveRoutes = new ArrayList<>();
        for (JsonNode node : routesNode) {
            String originCode = readText(node, "originCode", "origin", "from");
            String destinationCode = readText(node, "destinationCode", "destination", "to");
            int distanceKm = readInt(node, "distanceKm", "distance_km", "distance");
            int durationMinutes = readInt(node, "durationMinutes", "duration_minutes", "duration");

            if (originCode == null || destinationCode == null) {
                continue;
            }

            liveRoutes.add(new Route(
                    node.has("id") ? node.get("id").asText() : "EXT-" + originCode + "-" + destinationCode,
                    new Airport(originCode, originCode, "External"),
                    new Airport(destinationCode, destinationCode, "External"),
                    distanceKm > 0 ? distanceKm : 1800,
                    durationMinutes > 0 ? durationMinutes : 120
            ));
        }

        return liveRoutes;
    }

    private List<Route> fallbackRoutes() {
        List<Route> routes = new ArrayList<>();
        routes.add(new Route("EXT-GRU-REC", new Airport("GRU", "São Paulo", "Brazil"), new Airport("REC", "Recife", "Brazil"), 2800, 180));
        routes.add(new Route("EXT-GRU-SSA", new Airport("GRU", "São Paulo", "Brazil"), new Airport("SSA", "Salvador", "Brazil"), 2200, 150));
        routes.add(new Route("EXT-GIG-CWB", new Airport("GIG", "Rio de Janeiro", "Brazil"), new Airport("CWB", "Curitiba", "Brazil"), 1600, 120));
        routes.add(new Route("EXT-GRU-LIS", new Airport("GRU", "São Paulo", "Brazil"), new Airport("LIS", "Lisbon", "Portugal"), 9600, 420));
        return routes;
    }

    private String readText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull() && !node.get(key).asText().isBlank()) {
                return node.get(key).asText();
            }
        }
        return null;
    }

    private int readInt(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                try {
                    return node.get(key).asInt();
                } catch (Exception ignored) {
                    String value = node.get(key).asText();
                    try {
                        return Integer.parseInt(value);
                    } catch (Exception ignoredAgain) {
                        // fallback to next key
                    }
                }
            }
        }
        return 0;
    }
}
