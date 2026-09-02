package com.skypilot.backend.aviation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class FlightAwareClient {

    private static final Logger log = LoggerFactory.getLogger(FlightAwareClient.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final String apiKey;

    public FlightAwareClient(
            @Value("${aviation.data.base-url:}") String baseUrl,
            @Value("${aviation.data.api-key:}") String apiKey
    ) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public List<FlightAwareRoute> fetchRoutes() {
        return fetchRoutesForAirport("GRU");
    }

    public List<FlightAwareRoute> fetchRoutesForAirport(String airportCode) {
        if (airportCode == null || airportCode.isBlank()) {
            return List.of();
        }

        String url = resolveAirportFlightsUrl(airportCode);
        if (url.isBlank()) {
            return List.of();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("x-apikey", apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("FlightAware returned HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode flightsNode = root.has("flights") ? root.get("flights") : root;
            if (flightsNode == null || !flightsNode.isArray()) {
                return List.of();
            }

            List<FlightAwareRoute> routes = new ArrayList<>();
            for (JsonNode node : flightsNode) {
                String originCode = readText(node, "origin", "originCode", "from");
                String destinationCode = readText(node, "destination", "destinationCode", "to");
                int distanceKm = readInt(node, "distance", "distanceKm", "distance_km");
                int durationMinutes = readInt(node, "duration", "durationMinutes", "duration_minutes");
                String flightId = readText(node, "ident", "id", "flightId");
                String aircraftType = readText(node, "aircrafttype", "aircraftType", "aircraft_type");

                if (originCode == null || destinationCode == null) {
                    continue;
                }

                routes.add(new FlightAwareRoute(
                        flightId != null ? flightId : originCode + "-" + destinationCode,
                        originCode,
                        destinationCode,
                        distanceKm > 0 ? distanceKm : 1800,
                        durationMinutes > 0 ? durationMinutes : 120,
                        aircraftType,
                        "FLIGHTAWARE"
                ));
            }

            return routes;
        } catch (Exception ex) {
            log.warn("FlightAware integration failed for airport {}: {}", airportCode, ex.getMessage());
            return List.of();
        }
    }

    private String resolveAirportFlightsUrl(String airportCode) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }

        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String sanitizedAirport = URLEncoder.encode(airportCode.trim().toUpperCase(), StandardCharsets.UTF_8);
        return normalizedBaseUrl + "/airports/" + sanitizedAirport + "/flights?max_pages=1";
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
                    try {
                        return Integer.parseInt(node.get(key).asText());
                    } catch (Exception ignoredAgain) {
                        // fallback to next key
                    }
                }
            }
        }
        return 0;
    }

    public record FlightAwareRoute(String id, String originCode, String destinationCode, int distanceKm, int durationMinutes, String aircraftType, String source) {
        public FlightAwareRoute(String id, String originCode, String destinationCode, int distanceKm, int durationMinutes) {
            this(id, originCode, destinationCode, distanceKm, durationMinutes, null, "FLIGHTAWARE");
        }
    }
}
