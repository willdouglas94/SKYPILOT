package com.skypilot.backend;

import com.skypilot.backend.domain.*;
import com.skypilot.backend.service.*;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlightEligibilityEngineTest {

    @Test
    void shouldAllowEligibleFlightWhenPilotHasLicenseAndRouteMatchesAircraft() {
        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        Airport origin = new Airport("GRU", "São Paulo", "Brazil");
        Airport destination = new Airport("REC", "Recife", "Brazil");
        AircraftType a320 = new AircraftType("AT-1", "Airbus", "A320", "Narrowbody", 5000, 180);
        Aircraft aircraft = new Aircraft("AC-1", "PR-SKY", a320, airline, "ACTIVE");
        Route route = new Route("RT-1", origin, destination, 2800, 180);
        RouteAircraft routeAircraft = new RouteAircraft("RT-1", "AT-1", true);
        FlightSchedule schedule = new FlightSchedule("FS-1", route, "SP-100", DayOfWeek.MONDAY, "08:00", "10:00");

        Pilot pilot = new Pilot("pilot-1", "Ana", 0, 80, 18);
        pilot.setCurrentLocation("GRU");
        pilot.setLicenses(Set.of("AT-1"));

        FlightEligibilityEngine engine = new FlightEligibilityEngine();
        EligibilityResult result = engine.evaluate(pilot, route, aircraft, schedule, List.of(schedule));

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getReasons()).doesNotContain("PILOT_NOT_LICENSED");
    }

    @Test
    void shouldBlockRouteWhenPilotIsNotLicensedForAircraft() {
        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        Airport origin = new Airport("GRU", "São Paulo", "Brazil");
        Airport destination = new Airport("BSB", "Brasília", "Brazil");
        AircraftType b737 = new AircraftType("AT-2", "Boeing", "737", "Narrowbody", 4500, 160);
        Aircraft aircraft = new Aircraft("AC-2", "PR-BRA", b737, airline, "ACTIVE");
        Route route = new Route("RT-2", origin, destination, 1500, 120);
        RouteAircraft routeAircraft = new RouteAircraft("RT-2", "AT-2", true);
        FlightSchedule schedule = new FlightSchedule("FS-2", route, "SP-200", DayOfWeek.TUESDAY, "09:00", "11:00");

        Pilot pilot = new Pilot("pilot-2", "Bia", 0, 60, 10);
        pilot.setCurrentLocation("GRU");
        pilot.setLicenses(Set.of("AT-1"));

        FlightEligibilityEngine engine = new FlightEligibilityEngine();
        EligibilityResult result = engine.evaluate(pilot, route, aircraft, schedule, List.of(schedule));

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getReasons()).contains("PILOT_NOT_LICENSED");
    }

    @Test
    void shouldRejectWhenConflictExistsWithCurrentSchedule() {
        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        Airport origin = new Airport("GRU", "São Paulo", "Brazil");
        Airport destination = new Airport("REC", "Recife", "Brazil");
        AircraftType a320 = new AircraftType("AT-1", "Airbus", "A320", "Narrowbody", 5000, 180);
        Aircraft aircraft = new Aircraft("AC-1", "PR-SKY", a320, airline, "ACTIVE");
        Route route = new Route("RT-1", origin, destination, 2800, 180);
        FlightSchedule proposed = new FlightSchedule("FS-3", route, "SP-300", DayOfWeek.MONDAY, "08:00", "10:00");
        FlightSchedule conflict = new FlightSchedule("FS-4", route, "SP-400", DayOfWeek.MONDAY, "09:00", "11:00");

        Pilot pilot = new Pilot("pilot-3", "Luca", 0, 70, 16);
        pilot.setCurrentLocation("GRU");
        pilot.setLicenses(Set.of("AT-1"));

        FlightEligibilityEngine engine = new FlightEligibilityEngine();
        EligibilityResult result = engine.evaluate(pilot, route, aircraft, proposed, List.of(conflict));

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getReasons()).contains("SCHEDULE_CONFLICT");
    }

    @Test
    void shouldRejectFlightWhenAircraftRangeCannotCoverRouteDistance() {
        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        Airport origin = new Airport("GRU", "São Paulo", "Brazil");
        Airport destination = new Airport("LAX", "Los Angeles", "USA");
        AircraftType shortRange = new AircraftType("AT-3", "Airbus", "A319", "NARROWBODY", 1200, 150);
        Aircraft aircraft = new Aircraft("AC-3", "PR-SHORT", shortRange, airline, "ACTIVE");
        Route route = new Route("RT-3", origin, destination, 4200, 360);
        FlightSchedule schedule = new FlightSchedule("FS-5", route, "SP-500", DayOfWeek.WEDNESDAY, "07:00", "18:00");

        Pilot pilot = new Pilot("pilot-4", "Maya", 0, 85, 20);
        pilot.setCurrentLocation("GRU");
        pilot.setLicenses(Set.of("AT-3"));

        FlightEligibilityEngine engine = new FlightEligibilityEngine();
        EligibilityResult result = engine.evaluate(pilot, route, aircraft, schedule, List.of());

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getReasons()).contains("AIRCRAFT_NOT_COMPATIBLE_WITH_ROUTE");
    }

    @Test
    void shouldSelectOnlyValidOptionsInEmployerAi() {
        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        Airport gru = new Airport("GRU", "São Paulo", "Brazil");
        Airport rec = new Airport("REC", "Recife", "Brazil");
        Airport ssa = new Airport("SSA", "Salvador", "Brazil");
        AircraftType a320 = new AircraftType("AT-1", "Airbus", "A320", "Narrowbody", 5000, 180);
        Aircraft aircraft = new Aircraft("AC-1", "PR-SKY", a320, airline, "ACTIVE");
        Route routeOne = new Route("RT-1", gru, rec, 2800, 180);
        Route routeTwo = new Route("RT-2", gru, ssa, 2200, 150);

        FlightOffer offerOne = new FlightOffer(LocalDate.of(2026, 9, 18), routeOne, aircraft, "09:40");
        FlightOffer offerTwo = new FlightOffer(LocalDate.of(2026, 9, 18), routeTwo, aircraft, "12:10");

        RuleBasedEmployerAI ai = new RuleBasedEmployerAI();
        FlightOffer selected = ai.chooseOffer(List.of(offerOne, offerTwo));

        assertThat(List.of(offerOne, offerTwo)).contains(selected);
        assertThat(selected.getRoute().getDestination().getCode()).isIn("REC", "SSA");
    }

    @Test
    void shouldCreateOfferAndAcceptItThroughLifecycle() {
        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        Airport origin = new Airport("GRU", "São Paulo", "Brazil");
        Airport destination = new Airport("REC", "Recife", "Brazil");
        AircraftType a320 = new AircraftType("AT-1", "Airbus", "A320", "Narrowbody", 5000, 180);
        Aircraft aircraft = new Aircraft("AC-1", "PR-SKY", a320, airline, "ACTIVE");
        Route route = new Route("RT-1", origin, destination, 2800, 180);

        AviationCatalogService catalog = new AviationCatalogService();
        catalog.createAirline("SkyPilot", "SP", "SKP", "Brazil", "São Paulo", "GRU");
        catalog.createAircraftType("Airbus", "A320", "NARROWBODY", 5000, 180);
        catalog.createAircraft("PR-SKY", "AT-1", "airline-default", "ACTIVE");
        catalog.createRoute("GRU", "REC", 2800, 180);

        FlightOffer offer = catalog.createOffer(route.getId(), aircraft.getId(), "09:40", LocalDate.of(2026, 9, 18));
        assertThat(offer.getStatus()).isEqualTo("PENDING");

        FlightOffer accepted = catalog.updateOfferStatus(offer.getId(), "ACCEPTED");
        assertThat(accepted.getStatus()).isEqualTo("ACCEPTED");
        assertThat(catalog.listOffers()).extracting(FlightOffer::getStatus).contains("ACCEPTED");
    }

    @Test
    void shouldSeedDefaultCatalogAndExposeOperationalSummary() {
        AviationCatalogService catalog = new AviationCatalogService();
        catalog.initializeDefaults();

        assertThat(catalog.listAirlines()).isNotEmpty();
        assertThat(catalog.listAircraft()).isNotEmpty();
        assertThat(catalog.listRoutes()).isNotEmpty();

        Map<String, Object> summary = catalog.getDashboardSummary();
        assertThat(summary).containsKeys("airlines", "aircraft", "routes", "offers", "pendingOffers", "acceptedOffers", "rejectedOffers", "dispatchReadyOffers");
        assertThat(summary.get("airlines")).isInstanceOf(Number.class);
        assertThat(summary.get("rejectedOffers")).isInstanceOf(Number.class);
        assertThat(summary.get("dispatchReadyOffers")).isInstanceOf(Number.class);
    }

    @Test
    void shouldBuildDispatchBoardWithReadinessAndFlightWindow() {
        AviationCatalogService catalog = new AviationCatalogService();
        catalog.initializeDefaults();

        List<Map<String, Object>> board = catalog.getDispatchBoard();

        assertThat(board).isNotEmpty();
        assertThat(board.getFirst()).containsKeys("offerId", "routeCode", "aircraftRegistration", "status", "dispatchReady", "flightWindow");
        assertThat(board.getFirst().get("dispatchReady")).isInstanceOf(Boolean.class);
        assertThat(board.getFirst().get("flightWindow")).isNotNull();
    }

    @Test
    void shouldAdvanceAcrossRealFlightLifecycle() {
        AviationCatalogService catalog = new AviationCatalogService();
        catalog.createAirline("SkyPilot", "SP", "SKP", "Brazil", "São Paulo", "GRU");
        catalog.createAircraftType("Airbus", "A320", "NARROWBODY", 5000, 180);
        catalog.createAircraft("PR-SKY", "AT-1", "airline-default", "ACTIVE");
        catalog.createRoute("GRU", "REC", 2800, 180);

        FlightOffer offer = catalog.createOffer("route-default", "aircraft-default", "09:40", LocalDate.of(2026, 9, 18));

        catalog.updateOfferStatus(offer.getId(), "ACCEPTED");
        catalog.updateOfferStatus(offer.getId(), "DISPATCH_APPROVED");
        catalog.updateOfferStatus(offer.getId(), "DEPARTED");
        catalog.updateOfferStatus(offer.getId(), "ARRIVED");
        catalog.updateOfferStatus(offer.getId(), "COMPLETED");

        assertThat(catalog.listOffers().stream().filter(item -> item.getId().equals(offer.getId())).findFirst().orElseThrow().getStatus())
                .isEqualTo("COMPLETED");
    }

    @Test
    void shouldRejectIllegalFlightLifecycleTransition() {
        AviationCatalogService catalog = new AviationCatalogService();
        catalog.createAirline("SkyPilot", "SP", "SKP", "Brazil", "São Paulo", "GRU");
        catalog.createAircraftType("Airbus", "A320", "NARROWBODY", 5000, 180);
        catalog.createAircraft("PR-SKY", "AT-1", "airline-default", "ACTIVE");
        catalog.createRoute("GRU", "REC", 2800, 180);

        FlightOffer offer = catalog.createOffer("route-default", "aircraft-default", "09:40", LocalDate.of(2026, 9, 18));

        assertThatThrownBy(() -> catalog.updateOfferStatus(offer.getId(), "ARRIVED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }
}
