package com.skypilot.backend.service;

import com.skypilot.backend.domain.Aircraft;
import com.skypilot.backend.domain.FlightSchedule;
import com.skypilot.backend.domain.Pilot;
import com.skypilot.backend.domain.Route;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteRecommendationService {

    private final FlightEligibilityEngine eligibilityEngine;

    public RouteRecommendationService(FlightEligibilityEngine eligibilityEngine) {
        this.eligibilityEngine = eligibilityEngine == null ? new FlightEligibilityEngine() : eligibilityEngine;
    }

    public List<RouteRecommendation> recommendForPilot(
            Pilot pilot,
            List<Route> routes,
            List<Aircraft> aircraft,
            List<FlightSchedule> schedules
    ) {
        if (pilot == null) {
            throw new IllegalArgumentException("Piloto obrigatório");
        }

        List<RouteRecommendation> recommendations = new ArrayList<>();

        for (Route route : routes == null ? Collections.<Route>emptyList() : routes) {
            if (route == null) {
                continue;
            }

            Aircraft bestAircraft = aircraft == null ? null : aircraft.stream()
                    .filter(candidate -> candidate != null)
                    .filter(candidate -> candidate.getAircraftType() != null)
                    .filter(candidate -> candidate.getStatus() != null && !candidate.getStatus().isBlank())
                    .findFirst()
                    .orElse(null);

            if (bestAircraft == null) {
                continue;
            }

            FlightSchedule schedule = schedules == null || schedules.isEmpty() ? null : schedules.stream()
                    .filter(candidate -> candidate != null)
                    .filter(candidate -> candidate.getRoute() != null && candidate.getRoute().getId().equals(route.getId()))
                    .findFirst()
                    .orElse(null);

            EligibilityResult result = eligibilityEngine.evaluate(pilot, route, bestAircraft, schedule, schedules == null ? Collections.<FlightSchedule>emptyList() : schedules);
            if (!result.isEligible()) {
                continue;
            }

            int score = scoreRoute(pilot, route, bestAircraft, schedule);
            recommendations.add(new RouteRecommendation(route, score, List.of("ELIGIBLE")));
        }

        recommendations.sort(Comparator.comparingInt(RouteRecommendation::score).reversed());
        return recommendations;
    }

    private int scoreRoute(Pilot pilot, Route route, Aircraft aircraft, FlightSchedule schedule) {
        int score = 0;
        score += Math.max(0, pilot.getSkill());
        score += Math.max(0, route.getDistanceKm() / 50);
        score += Math.max(0, 100 - route.getDistanceKm() / 150);
        score += aircraft.getAircraftType() != null ? 25 : 0;
        score += schedule != null ? 10 : 0;
        return score;
    }

    public record RouteRecommendation(Route route, int score, List<String> reasons) {
    }
}
