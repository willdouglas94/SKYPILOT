package com.skypilot.backend.service;

import com.skypilot.backend.domain.Aircraft;
import com.skypilot.backend.domain.AircraftType;
import com.skypilot.backend.domain.Airline;
import com.skypilot.backend.domain.FlightOffer;
import com.skypilot.backend.domain.Route;
import com.skypilot.backend.domain.RouteAircraft;
import com.skypilot.backend.domain.Pilot;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AviationCatalogService {
    private final PilotPerformanceService pilotPerformanceService;
    private final ExternalFlightDataService externalFlightDataService;
    private final Map<String, Airline> airlines = new ConcurrentHashMap<>();
    private final Map<String, AircraftType> aircraftTypes = new ConcurrentHashMap<>();
    private final Map<String, Aircraft> aircraft = new ConcurrentHashMap<>();
    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final Map<String, RouteAircraft> routeAircraftMap = new ConcurrentHashMap<>();
    private final Map<String, FlightOffer> offers = new ConcurrentHashMap<>();

    public AviationCatalogService() {
        this(new PilotPerformanceService(), new ExternalFlightDataService(
                new com.skypilot.backend.aviation.client.FlightAwareClient("", ""),
                new com.skypilot.backend.aviation.mapper.FlightAwareMapper(),
                null
        ));
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AviationCatalogService(PilotPerformanceService pilotPerformanceService, ExternalFlightDataService externalFlightDataService) {
        this.pilotPerformanceService = pilotPerformanceService;
        this.externalFlightDataService = externalFlightDataService;
    }

    @PostConstruct
    public void initializeDefaults() {
        if (!airlines.isEmpty() || !aircraftTypes.isEmpty() || !aircraft.isEmpty() || !routes.isEmpty()) {
            return;
        }

        Airline skyjet = createAirline("SkyJet", "SJ", "SJT", "Brazil", "São Paulo", "GRU");
        Airline azul = createAirline("Azul", "AD", "AZU", "Brazil", "São Paulo", "VCP");
        Airline latam = createAirline("LATAM", "LA", "LAA", "Brazil", "Rio de Janeiro", "GIG");

        AircraftType a320 = createAircraftType("Airbus", "A320", "NARROWBODY", 5000, 180);
        AircraftType b737 = createAircraftType("Boeing", "737-800", "NARROWBODY", 4500, 170);

        Aircraft ac1 = createAircraft("PR-SJ01", a320.getId(), skyjet.getId(), "ACTIVE");
        Aircraft ac2 = createAircraft("PR-AZ11", b737.getId(), azul.getId(), "ACTIVE");
        Aircraft ac3 = createAircraft("PR-LA22", a320.getId(), latam.getId(), "ACTIVE");

        externalFlightDataService.fetchRoutes().forEach(route -> {
            routes.put(route.getId(), route);
        });

        Route gruRec = routes.getOrDefault("EXT-GRU-REC", createRoute("GRU", "REC", 2800, 180));
        Route gruSsa = routes.getOrDefault("EXT-GRU-SSA", createRoute("GRU", "SSA", 2100, 150));
        Route gigCwb = routes.getOrDefault("EXT-GIG-CWB", createRoute("GIG", "CWB", 1600, 120));

        createOffer(gruRec.getId(), ac1.getId(), "09:40", LocalDate.now().plusDays(1));
        createOffer(gruSsa.getId(), ac2.getId(), "12:10", LocalDate.now().plusDays(2));
        createOffer(gigCwb.getId(), ac3.getId(), "14:25", LocalDate.now().plusDays(3));
    }

    public Airline createAirline(String name, String iata, String icao, String country, String baseCity, String mainAirportCode) {
        Airline airline = new Airline(java.util.UUID.randomUUID().toString(), name, iata, icao, country, baseCity, mainAirportCode, "USER_CREATED");
        airlines.put(airline.getId(), airline);
        return airline;
    }

    public List<Airline> listAirlines() {
        return new ArrayList<>(airlines.values());
    }

    public AircraftType createAircraftType(String manufacturer, String model, String category, int rangeKm, int capacity) {
        AircraftType type = new AircraftType(java.util.UUID.randomUUID().toString(), manufacturer, model, category, rangeKm, capacity);
        aircraftTypes.put(type.getId(), type);
        return type;
    }

    public List<AircraftType> listAircraftTypes() {
        return new ArrayList<>(aircraftTypes.values());
    }

    public Aircraft createAircraft(String registration, String typeId, String airlineId, String status) {
        AircraftType type = aircraftTypes.get(typeId);
        if (type == null) {
            type = aircraftTypes.values().stream().findFirst().orElseGet(() -> createAircraftType("Airbus", "A320", "NARROWBODY", 5000, 180));
        }

        Airline airline = airlines.get(airlineId);
        if (airline == null) {
            airline = airlines.values().stream().findFirst().orElseGet(() -> createAirline("SkyPilot", "SP", "SKP", "Brazil", "São Paulo", "GRU"));
        }

        Aircraft aircraftEntity = new Aircraft(java.util.UUID.randomUUID().toString(), registration, type, airline, status);
        aircraft.put(aircraftEntity.getId(), aircraftEntity);
        return aircraftEntity;
    }

    public List<Aircraft> listAircraft() {
        return new ArrayList<>(aircraft.values());
    }

    public Route createRoute(String originCode, String destinationCode, int distanceKm, int durationMinutes) {
        Route route = new Route(java.util.UUID.randomUUID().toString(),
                new com.skypilot.backend.domain.Airport(originCode, originCode, "Brazil"),
                new com.skypilot.backend.domain.Airport(destinationCode, destinationCode, "Brazil"),
                distanceKm, durationMinutes);
        routes.put(route.getId(), route);
        return route;
    }

    public List<Route> listRoutes() {
        return new ArrayList<>(routes.values());
    }

    public RouteAircraft assignRouteToAircraftType(String routeId, String aircraftTypeId, boolean compatible) {
        RouteAircraft routeAircraft = new RouteAircraft(routeId, aircraftTypeId, compatible);
        routeAircraftMap.put(routeId + ":" + aircraftTypeId, routeAircraft);
        return routeAircraft;
    }

    public List<RouteAircraft> listRouteAircraftAssignments() {
        return new ArrayList<>(routeAircraftMap.values());
    }

    public FlightOffer createOffer(String routeId, String aircraftId, String departureTime, LocalDate date) {
        Route route = routes.get(routeId);
        if (route == null) {
            route = routes.values().stream().findFirst().orElseGet(() -> createRoute("GRU", "REC", 2800, 180));
        }

        Aircraft aircraftEntity = aircraft.get(aircraftId);
        if (aircraftEntity == null) {
            aircraftEntity = aircraft.values().stream().findFirst().orElseGet(() -> createAircraft("PR-SKY", "", "", "ACTIVE"));
        }

        FlightOffer offer = new FlightOffer(date, route, aircraftEntity, departureTime);
        offers.put(offer.getId(), offer);
        return offer;
    }

    public List<FlightOffer> listOffers() {
        return new ArrayList<>(offers.values());
    }

    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        int pendingOffers = (int) offers.values().stream().filter(offer -> "PENDING".equals(offer.getStatus())).count();
        int acceptedOffers = (int) offers.values().stream().filter(offer -> "ACCEPTED".equals(offer.getStatus())).count();
        int rejectedOffers = (int) offers.values().stream().filter(offer -> "REJECTED".equals(offer.getStatus())).count();

        summary.put("airlines", airlines.size());
        summary.put("aircraft", aircraft.size());
        summary.put("routes", routes.size());
        summary.put("offers", offers.size());
        summary.put("pendingOffers", pendingOffers);
        summary.put("acceptedOffers", acceptedOffers);
        summary.put("rejectedOffers", rejectedOffers);
        summary.put("dispatchReadyOffers", pendingOffers + acceptedOffers);
        return summary;
    }

    public List<Map<String, Object>> getDispatchBoard() {
        return offers.values().stream()
                .sorted(Comparator.comparing(FlightOffer::getDate).thenComparing(FlightOffer::getDepartureTime))
                .map(offer -> {
                    String routeCode = offer.getRoute().getOrigin().getCode() + " → " + offer.getRoute().getDestination().getCode();
                    boolean dispatchReady = "PENDING".equals(offer.getStatus()) || "ACCEPTED".equals(offer.getStatus());
                    String flightWindow = offer.getDate() + " · " + offer.getDepartureTime() + " · " + offer.getRoute().getDurationMinutes() + " min";

                    Map<String, Object> item = new HashMap<>();
                    item.put("offerId", offer.getId());
                    item.put("routeCode", routeCode);
                    item.put("aircraftRegistration", offer.getAircraft() != null ? offer.getAircraft().getRegistration() : "N/A");
                    item.put("status", offer.getStatus());
                    item.put("dispatchReady", dispatchReady);
                    item.put("flightWindow", flightWindow);
                    return item;
                })
                .toList();
    }

    public FlightOffer updateOfferStatus(String offerId, String status) {
        FlightOffer offer = offers.get(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("Oferta não encontrada");
        }

        String normalizedStatus = normalizeStatus(status);
        validateStatusTransition(offer.getStatus(), normalizedStatus);
        offer.setStatus(normalizedStatus);
        return offer;
    }

    public FlightOffer assignPilotToOffer(String offerId, String pilotId) {
        FlightOffer offer = offers.get(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("Oferta não encontrada");
        }
        if (pilotId == null || pilotId.isBlank()) {
            throw new IllegalArgumentException("Piloto obrigatório");
        }

        offer.setAssignedPilotId(pilotId);
        return offer;
    }

    public FlightOffer getOffer(String offerId) {
        FlightOffer offer = offers.get(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("Oferta não encontrada");
        }
        return offer;
    }

    public FlightOffer completeFlight(String offerId, int score, String notes) {
        FlightOffer offer = offers.get(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("Oferta não encontrada");
        }
        if (offer.getAssignedPilotId() == null || offer.getAssignedPilotId().isBlank()) {
            throw new IllegalStateException("É necessário atribuir um piloto antes de concluir o voo.");
        }

        updateOfferStatus(offerId, "ARRIVED");
        updateOfferStatus(offerId, "COMPLETED");
        offer.setCompletionScore(score);
        offer.setCompletionNotes(notes);
        pilotPerformanceService.recordCompletedFlight(offer.getAssignedPilotId(), offer, score, notes == null ? "Voo concluído" : notes);
        return offer;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDING";
        }
        return status.trim().toUpperCase();
    }

    private void validateStatusTransition(String currentStatus, String nextStatus) {
        String current = currentStatus == null ? "PENDING" : currentStatus.trim().toUpperCase();
        String next = nextStatus == null ? "PENDING" : nextStatus.trim().toUpperCase();

        if (current.equals(next)) {
            return;
        }

        Map<String, List<String>> allowedTransitions = Map.of(
                "PENDING", List.of("ACCEPTED", "REJECTED"),
                "ACCEPTED", List.of("DISPATCH_APPROVED", "REJECTED"),
                "DISPATCH_APPROVED", List.of("DEPARTED"),
                "DEPARTED", List.of("ARRIVED"),
                "ARRIVED", List.of("COMPLETED"),
                "REJECTED", List.of(),
                "COMPLETED", List.of()
        );

        List<String> validTransitions = allowedTransitions.getOrDefault(current, List.of());
        if (!validTransitions.contains(next)) {
            throw new IllegalStateException("Transição inválida de " + current + " para " + next + ". Status permitido a partir de " + current + ": " + validTransitions);
        }
    }

    public List<FlightOffer> generateOffersForPilot(Pilot pilot) {
        if (pilot == null) {
            return Collections.emptyList();
        }

        List<FlightOffer> offersList = new ArrayList<>();
        for (Route route : routes.values()) {
            List<Aircraft> compatibleAircraft = aircraft.values().stream()
                    .filter(item -> item.getAircraftType() != null && item.getStatus() != null)
                    .toList();

            for (Aircraft a : compatibleAircraft) {
                offersList.add(new FlightOffer(LocalDate.now().plusDays(1), route, a, "09:40"));
            }
        }
        return offersList;
    }
}
