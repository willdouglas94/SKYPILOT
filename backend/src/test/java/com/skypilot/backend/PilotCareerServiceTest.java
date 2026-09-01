package com.skypilot.backend;

import com.skypilot.backend.domain.Pilot;
import com.skypilot.backend.dto.RaceRequest;
import com.skypilot.backend.dto.RaceResult;
import com.skypilot.backend.repository.PilotRepository;
import com.skypilot.backend.service.AviationCatalogService;
import com.skypilot.backend.service.PilotCareerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PilotCareerServiceTest {

    @Autowired
    private PilotCareerService service;

    @Autowired
    private PilotRepository pilotRepository;

    @Autowired
    private AviationCatalogService catalog;

    @Test
    void shouldCreatePilotWithDefaultState() {
        Pilot pilot = service.createPilot("Luca");

        assertThat(pilot.getId()).isNotNull();
        assertThat(pilot.getName()).isEqualTo("Luca");
        assertThat(pilot.getPoints()).isZero();
        assertThat(pilot.getSkill()).isGreaterThan(0);
        assertThat(pilot.getExperience()).isGreaterThan(0);
        assertThat(pilotRepository.findById(pilot.getId())).isPresent();
    }

    @Test
    void shouldSimulateRaceAndAwardPointsToWinner() {
        Pilot pilotA = service.createPilot("Ana");
        Pilot pilotB = service.createPilot("Beto");

        RaceResult result = service.simulateRace(new RaceRequest(pilotA.getId(), pilotB.getId()));

        assertThat(result.getWinnerId()).isIn(pilotA.getId(), pilotB.getId());
        assertThat(result.getWinnerPoints()).isGreaterThanOrEqualTo(10);
        assertThat(result.getLoserPoints()).isGreaterThanOrEqualTo(0);
        assertThat(result.getSummary()).contains("venceu");
    }

    @Test
    void shouldTrackCompletedFlightHistoryAndSummary() {
        var pilot = new Pilot("pilot-101", "Lina", 120, 85, 18, "GRU");
        var airline = new com.skypilot.backend.domain.Airline("airline-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        var origin = new com.skypilot.backend.domain.Airport("GRU", "São Paulo", "Brazil");
        var destination = new com.skypilot.backend.domain.Airport("REC", "Recife", "Brazil");
        var aircraftType = new com.skypilot.backend.domain.AircraftType("type-1", "Airbus", "A320", "NARROWBODY", 5000, 180);
        var aircraft = new com.skypilot.backend.domain.Aircraft("aircraft-1", "PR-SKY", aircraftType, airline, "ACTIVE");
        var route = new com.skypilot.backend.domain.Route("route-1", origin, destination, 2800, 180);
        var offer = new com.skypilot.backend.domain.FlightOffer(java.time.LocalDate.of(2026, 9, 18), route, aircraft, "09:40");

        var performance = new com.skypilot.backend.service.PilotPerformanceService();
        performance.recordCompletedFlight(pilot.getId(), offer, 94, "Pouso dentro do cronograma");

        assertThat(performance.getHistory(pilot.getId())).hasSize(1);
        assertThat(performance.getHistory(pilot.getId()).getFirst().getRouteCode()).isEqualTo("GRU → REC");
        assertThat(performance.getSummary(pilot.getId()).get("completedFlights")).isEqualTo(1);
        assertThat(performance.getSummary(pilot.getId()).get("averageScore")).isEqualTo(94.0);
    }

    @Test
    void shouldRankPilotsByCareerAndPerformance() {
        Pilot pilotA = service.createPilot("Ana");
        Pilot pilotB = service.createPilot("Beto");

        var airline = new com.skypilot.backend.domain.Airline("airline-rank", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        var origin = new com.skypilot.backend.domain.Airport("GRU", "São Paulo", "Brazil");
        var destination = new com.skypilot.backend.domain.Airport("REC", "Recife", "Brazil");
        var aircraftType = new com.skypilot.backend.domain.AircraftType("type-rank", "Airbus", "A320", "NARROWBODY", 5000, 180);
        var aircraft = new com.skypilot.backend.domain.Aircraft("aircraft-rank", "PR-RANK", aircraftType, airline, "ACTIVE");
        var route = new com.skypilot.backend.domain.Route("route-rank", origin, destination, 2800, 180);

        var offerOne = new com.skypilot.backend.domain.FlightOffer(java.time.LocalDate.of(2026, 9, 18), route, aircraft, "09:40");
        var offerTwo = new com.skypilot.backend.domain.FlightOffer(java.time.LocalDate.of(2026, 9, 19), route, aircraft, "11:20");
        var offerThree = new com.skypilot.backend.domain.FlightOffer(java.time.LocalDate.of(2026, 9, 20), route, aircraft, "12:50");

        service.recordCompletedFlight(pilotA.getId(), offerOne, 95, "Excelente operação");
        service.recordCompletedFlight(pilotA.getId(), offerTwo, 92, "Voo estável");
        service.recordCompletedFlight(pilotB.getId(), offerThree, 81, "Operação aceitável");

        var ranking = service.getRanking();

        assertThat(ranking).isNotEmpty();
        assertThat(ranking.getFirst().get("pilotId")).isEqualTo(pilotA.getId());
        assertThat(((Number) ranking.getFirst().get("reputation")).intValue())
                .isGreaterThan(((Number) ranking.get(1).get("reputation")).intValue());
    }

    @Test
    void shouldCompleteOfferAndRecordPilotPerformance() {
        Pilot pilot = service.createPilot("Clara");
        var route = catalog.listRoutes().getFirst();
        var aircraft = catalog.listAircraft().getFirst();
        var offer = catalog.createOffer(route.getId(), aircraft.getId(), "09:40", java.time.LocalDate.of(2026, 9, 18));

        catalog.assignPilotToOffer(offer.getId(), pilot.getId());
        catalog.updateOfferStatus(offer.getId(), "ACCEPTED");
        catalog.updateOfferStatus(offer.getId(), "DISPATCH_APPROVED");
        catalog.updateOfferStatus(offer.getId(), "DEPARTED");
        catalog.updateOfferStatus(offer.getId(), "ARRIVED");
        catalog.completeFlight(offer.getId(), 97, "Operação segura e pontual");

        assertThat(offer.getStatus()).isEqualTo("COMPLETED");
        assertThat(catalog.getOffer(offer.getId()).getAssignedPilotId()).isEqualTo(pilot.getId());

        var ranking = service.getRanking();
        assertThat(ranking).anySatisfy(row -> {
            if (pilot.getId().equals(row.get("pilotId"))) {
                assertThat(((Number) row.get("completedFlights")).intValue()).isGreaterThan(0);
            }
        });
    }
}
