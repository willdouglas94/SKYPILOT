package com.skypilot.backend.controller;

import com.skypilot.backend.domain.Aircraft;
import com.skypilot.backend.domain.AircraftType;
import com.skypilot.backend.domain.Airline;
import com.skypilot.backend.domain.FlightOffer;
import com.skypilot.backend.domain.Route;
import com.skypilot.backend.service.AviationCatalogService;
import com.skypilot.backend.service.ExternalFlightDataService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AviationController {

    private final AviationCatalogService aviationCatalogService;
    private final ExternalFlightDataService externalFlightDataService;

    public AviationController(AviationCatalogService aviationCatalogService, ExternalFlightDataService externalFlightDataService) {
        this.aviationCatalogService = aviationCatalogService;
        this.externalFlightDataService = externalFlightDataService;
    }

    @GetMapping("/airlines")
    public List<Airline> listAirlines() {
        return aviationCatalogService.listAirlines();
    }

    @PostMapping("/airlines")
    public Airline createAirline(@RequestBody Map<String, String> payload) {
        return aviationCatalogService.createAirline(
                payload.getOrDefault("name", "SkyPilot"),
                payload.getOrDefault("iata", "SP"),
                payload.getOrDefault("icao", "SKP"),
                payload.getOrDefault("country", "Brazil"),
                payload.getOrDefault("baseCity", "São Paulo"),
                payload.getOrDefault("mainAirportCode", "GRU")
        );
    }

    @GetMapping("/aircraft-types")
    public List<AircraftType> listAircraftTypes() {
        return aviationCatalogService.listAircraftTypes();
    }

    @PostMapping("/aircraft-types")
    public AircraftType createAircraftType(@RequestBody Map<String, String> payload) {
        return aviationCatalogService.createAircraftType(
                payload.getOrDefault("manufacturer", "Airbus"),
                payload.getOrDefault("model", "A320"),
                payload.getOrDefault("category", "NARROWBODY"),
                Integer.parseInt(payload.getOrDefault("rangeKm", "5000")),
                Integer.parseInt(payload.getOrDefault("capacity", "180"))
        );
    }

    @GetMapping("/aircraft")
    public List<Aircraft> listAircraft() {
        return aviationCatalogService.listAircraft();
    }

    @PostMapping("/aircraft")
    public Aircraft createAircraft(@RequestBody Map<String, String> payload) {
        return aviationCatalogService.createAircraft(
                payload.getOrDefault("registration", "PR-SKY"),
                payload.getOrDefault("typeId", ""),
                payload.getOrDefault("airlineId", ""),
                payload.getOrDefault("status", "ACTIVE")
        );
    }

    @GetMapping("/routes")
    public List<Route> listRoutes() {
        return aviationCatalogService.listRoutes();
    }

    @GetMapping("/routes/external")
    public List<Route> listExternalRoutes() {
        return externalFlightDataService.fetchRoutes();
    }

    @PostMapping("/routes")
    public Route createRoute(@RequestBody Map<String, String> payload) {
        return aviationCatalogService.createRoute(
                payload.getOrDefault("originCode", "GRU"),
                payload.getOrDefault("destinationCode", "REC"),
                Integer.parseInt(payload.getOrDefault("distanceKm", "2800")),
                Integer.parseInt(payload.getOrDefault("durationMinutes", "180"))
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return aviationCatalogService.getDashboardSummary();
    }

    @GetMapping("/dispatch-board")
    public List<Map<String, Object>> dispatchBoard() {
        return aviationCatalogService.getDispatchBoard();
    }

    @GetMapping("/offers")
    public List<FlightOffer> listOffers() {
        return aviationCatalogService.listOffers();
    }

    @PostMapping("/offers")
    public FlightOffer createOffer(@RequestBody Map<String, Object> payload) {
        String routeId = String.valueOf(payload.getOrDefault("routeId", ""));
        String aircraftId = String.valueOf(payload.getOrDefault("aircraftId", ""));
        String departureTime = String.valueOf(payload.getOrDefault("departureTime", "09:40"));
        LocalDate date = payload.get("date") == null ? LocalDate.now() : LocalDate.parse(String.valueOf(payload.get("date")));
        return aviationCatalogService.createOffer(routeId, aircraftId, departureTime, date);
    }

    @PatchMapping("/offers/{id}/status")
    public FlightOffer updateOfferStatus(@PathVariable String id, @RequestBody Map<String, String> payload) {
        return aviationCatalogService.updateOfferStatus(id, payload.getOrDefault("status", "PENDING"));
    }

    @PatchMapping("/offers/{id}/assign-pilot")
    public FlightOffer assignPilotToOffer(@PathVariable String id, @RequestBody Map<String, String> payload) {
        return aviationCatalogService.assignPilotToOffer(id, payload.get("pilotId"));
    }

    @PostMapping("/offers/{id}/complete")
    public FlightOffer completeFlight(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        int score = payload.get("score") == null ? 0 : Integer.parseInt(String.valueOf(payload.get("score")));
        String notes = payload.get("notes") == null ? "Voo concluído" : String.valueOf(payload.get("notes"));
        return aviationCatalogService.completeFlight(id, score, notes);
    }
}
