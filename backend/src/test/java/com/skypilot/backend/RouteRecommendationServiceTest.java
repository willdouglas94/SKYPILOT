package com.skypilot.backend;

import com.skypilot.backend.domain.*;
import com.skypilot.backend.service.FlightEligibilityEngine;
import com.skypilot.backend.service.RouteRecommendationService;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RouteRecommendationServiceTest {

    @Test
    void shouldReturnEligibleAndRankedRoutesForPilot() {
        Pilot pilot = new Pilot("pilot-1", "Ana", 0, 82, 18, "GRU");
        pilot.setLicenses(Set.of("AT-1"));

        Airline airline = new Airline("AZ-1", "SkyPilot", "SP", "SKP", "Brazil", "GRU");
        AircraftType a320 = new AircraftType("AT-1", "Airbus", "A320", "NARROWBODY", 5000, 180);
        Aircraft aircraft = new Aircraft("AC-1", "PR-SKY", a320, airline, "ACTIVE");

        Route routeOne = new Route("RT-1", new Airport("GRU", "São Paulo", "Brazil"), new Airport("REC", "Recife", "Brazil"), 2800, 180);
        Route routeTwo = new Route("RT-2", new Airport("GRU", "São Paulo", "Brazil"), new Airport("SSA", "Salvador", "Brazil"), 2200, 150);
        Route routeThree = new Route("RT-3", new Airport("GRU", "São Paulo", "Brazil"), new Airport("LAX", "Los Angeles", "USA"), 6200, 390);

        FlightSchedule scheduleOne = new FlightSchedule("FS-1", routeOne, "SP-101", DayOfWeek.MONDAY, "09:00", "12:00");
        FlightSchedule scheduleTwo = new FlightSchedule("FS-2", routeTwo, "SP-102", DayOfWeek.TUESDAY, "10:00", "12:30");

        RouteRecommendationService service = new RouteRecommendationService(new FlightEligibilityEngine());

        List<RouteRecommendationService.RouteRecommendation> recommendations = service.recommendForPilot(
                pilot,
                List.of(routeOne, routeTwo, routeThree),
                List.of(aircraft),
                List.of(scheduleOne, scheduleTwo)
        );

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations.get(0).route().getDestination().getCode()).isEqualTo("REC");
        assertThat(recommendations.get(0).score()).isGreaterThan(recommendations.get(1).score());
        assertThat(recommendations.get(0).reasons()).contains("ELIGIBLE");
    }

    @Test
    void shouldAutoGrantAircraftLicensesWhenPilotDoesNotHaveThem() {
        Pilot pilot = new Pilot("pilot-2", "Bia", 0, 88, 21, "GRU");
        pilot.setLicenses(Set.of());

        Airline airline = new Airline("AZ-2", "Skypilot", "SP", "SKP", "Brazil", "GRU");
        AircraftType a320 = new AircraftType("AT-2", "Airbus", "A320", "NARROWBODY", 5000, 180);
        Aircraft aircraft = new Aircraft("AC-2", "PR-SKY-2", a320, airline, "ACTIVE");

        Route routeOne = new Route("RT-4", new Airport("GRU", "São Paulo", "Brazil"), new Airport("REC", "Recife", "Brazil"), 2800, 180);
        Route routeTwo = new Route("RT-5", new Airport("GRU", "São Paulo", "Brazil"), new Airport("SSA", "Salvador", "Brazil"), 2200, 150);

        FlightSchedule scheduleOne = new FlightSchedule("FS-3", routeOne, "SP-103", DayOfWeek.MONDAY, "09:00", "12:00");
        FlightSchedule scheduleTwo = new FlightSchedule("FS-4", routeTwo, "SP-104", DayOfWeek.TUESDAY, "10:00", "12:30");

        RouteRecommendationService service = new RouteRecommendationService(new FlightEligibilityEngine());

        List<RouteRecommendationService.RouteRecommendation> recommendations = service.recommendForPilot(
                pilot,
                List.of(routeOne, routeTwo),
                List.of(aircraft),
                List.of(scheduleOne, scheduleTwo)
        );

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations.get(0).route().getDestination().getCode()).isEqualTo("REC");
    }
}
