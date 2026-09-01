package com.skypilot.backend.controller;

import com.skypilot.backend.domain.Pilot;
import com.skypilot.backend.dto.RaceRequest;
import com.skypilot.backend.dto.RaceResult;
import com.skypilot.backend.service.AviationCatalogService;
import com.skypilot.backend.service.PilotCareerService;
import com.skypilot.backend.service.RouteRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PilotController {

    private final PilotCareerService service;
    private final AviationCatalogService aviationCatalogService;
    private final RouteRecommendationService routeRecommendationService;

    public PilotController(PilotCareerService service, AviationCatalogService aviationCatalogService, RouteRecommendationService routeRecommendationService) {
        this.service = service;
        this.aviationCatalogService = aviationCatalogService;
        this.routeRecommendationService = routeRecommendationService;
    }

    @GetMapping("/pilots")
    public List<Pilot> listPilots() {
        return service.getPilots();
    }

    @PostMapping("/pilots")
    public Pilot createPilot(@RequestBody Map<String, String> payload) {
        String name = payload.getOrDefault("name", "Piloto");
        return service.createPilot(name);
    }

    @GetMapping("/pilot-ranking")
    public List<Map<String, Object>> getPilotRanking() {
        return service.getRanking();
    }

    @GetMapping("/pilots/{pilotId}/recommendations")
    public List<RouteRecommendationService.RouteRecommendation> getPilotRecommendations(@PathVariable String pilotId) {
        Pilot pilot = service.getPilots().stream()
                .filter(candidate -> candidate.getId().equals(pilotId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Piloto não encontrado"));

        return routeRecommendationService.recommendForPilot(
                pilot,
                aviationCatalogService.listRoutes(),
                aviationCatalogService.listAircraft(),
                java.util.Collections.emptyList()
        );
    }

    @PostMapping("/races")
    public RaceResult simulateRace(@RequestBody RaceRequest request) {
        return service.simulateRace(request);
    }
}
