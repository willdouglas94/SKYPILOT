package com.skypilot.backend.aviation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        String url = resolveRoutesUrl();
        if (url.isBlank()) {
            return List.of();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("FlightAware returned HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode routesNode = root.has("routes") ? root.get("routes") : root;
            if (routesNode == null || !routesNode.isArray()) {
                return List.of();
            }

            List<FlightAwareRoute> routes = new ArrayList<>();
            for (JsonNode node : routesNode) {
                String originCode = readText(node, "originCode", "origin", "from");
                String destinationCode = readText(node, "destinationCode", "destination", "to");
                int distanceKm = readInt(node, "distanceKm", "distance_km", "distance");
                int durationMinutes = readInt(node, "durationMinutes", "duration_minutes", "duration");

                if (originCode == null || destinationCode == null) {
                    continue;
                }

                routes.add(new FlightAwareRoute(
                        readText(node, "id", "routeId", "flightId"),
                        originCode,
                        destinationCode,
                        distanceKm > 0 ? distanceKm : 1800,
                        durationMinutes > 0 ? durationMinutes : 120
                ));
            }

            return routes;
        } catch (Exception ex) {
            log.warn("FlightAware integration failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private String resolveRoutesUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + "/routes";
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

    public record FlightAwareRoute(String id, String originCode, String destinationCode, int distanceKm, int durationMinutes) {
    }
}
