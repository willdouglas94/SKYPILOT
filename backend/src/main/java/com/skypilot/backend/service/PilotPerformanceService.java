package com.skypilot.backend.service;

import com.skypilot.backend.domain.FlightOffer;
import com.skypilot.backend.domain.PilotFlightRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PilotPerformanceService {

    private final Map<String, List<PilotFlightRecord>> flightHistory = new ConcurrentHashMap<>();

    public void recordCompletedFlight(String pilotId, FlightOffer offer, int score, String notes) {
        if (pilotId == null || pilotId.isBlank()) {
            throw new IllegalArgumentException("Pilot id obrigatório");
        }
        if (offer == null) {
            throw new IllegalArgumentException("Oferta obrigatória");
        }

        String routeCode = offer.getRoute() == null
                ? "N/A"
                : (offer.getRoute().getOrigin() == null ? "" : offer.getRoute().getOrigin().getCode())
                + " → " + (offer.getRoute().getDestination() == null ? "" : offer.getRoute().getDestination().getCode());

        PilotFlightRecord record = new PilotFlightRecord(
                UUID.randomUUID().toString(),
                pilotId,
                routeCode,
                offer.getDate() == null ? LocalDate.now() : offer.getDate(),
                score,
                notes
        );

        flightHistory.computeIfAbsent(pilotId, key -> new ArrayList<>()).add(record);
        flightHistory.get(pilotId).sort(Comparator.comparing(PilotFlightRecord::getFlightDate).reversed());
    }

    public List<PilotFlightRecord> getHistory(String pilotId) {
        if (pilotId == null || pilotId.isBlank()) {
            return List.of();
        }
        return List.copyOf(flightHistory.getOrDefault(pilotId, List.of()));
    }

    public Map<String, Object> getSummary(String pilotId) {
        List<PilotFlightRecord> history = getHistory(pilotId);
        if (history.isEmpty()) {
            return Map.of(
                    "completedFlights", 0,
                    "averageScore", 0.0,
                    "lastRouteCode", "N/A"
            );
        }

        double averageScore = history.stream()
                .mapToInt(PilotFlightRecord::getScore)
                .average()
                .orElse(0.0);

        return Map.of(
                "completedFlights", history.size(),
                "averageScore", averageScore,
                "lastRouteCode", history.getFirst().getRouteCode()
        );
    }
}
