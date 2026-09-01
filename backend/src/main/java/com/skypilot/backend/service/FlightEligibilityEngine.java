package com.skypilot.backend.service;

import com.skypilot.backend.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FlightEligibilityEngine {

    public EligibilityResult evaluate(Pilot pilot, Route route, Aircraft aircraft, FlightSchedule schedule, List<FlightSchedule> existingSchedules) {
        List<String> reasons = new ArrayList<>();

        if (pilot == null) {
            reasons.add("PILOT_MISSING");
        }
        if (route == null) {
            reasons.add("ROUTE_MISSING");
        }
        if (aircraft == null) {
            reasons.add("AIRCRAFT_MISSING");
        }
        if (schedule == null) {
            reasons.add("SCHEDULE_MISSING");
        }

        if (pilot != null && route != null && aircraft != null && schedule != null) {
            if (!isAircraftCompatibleWithRoute(aircraft, route)) {
                reasons.add("AIRCRAFT_NOT_COMPATIBLE_WITH_ROUTE");
            }

            if (!hasLicense(pilot, aircraft)) {
                reasons.add("PILOT_NOT_LICENSED");
            }

            if (!isPilotAtCorrectLocation(pilot, route)) {
                reasons.add("PILOT_LOCATION_MISMATCH");
            }

            if (hasConflict(schedule, existingSchedules)) {
                reasons.add("SCHEDULE_CONFLICT");
            }
        }

        return new EligibilityResult(reasons.isEmpty(), reasons);
    }

    private boolean isAircraftCompatibleWithRoute(Aircraft aircraft, Route route) {
        if (aircraft == null || route == null) {
            return false;
        }
        if (aircraft.getAircraftType() == null || route.getOrigin() == null || route.getDestination() == null) {
            return false;
        }

        int requiredRange = route.getDistanceKm();
        int aircraftRange = aircraft.getAircraftType().getRangeKm();
        return aircraftRange >= requiredRange;
    }

    private boolean hasLicense(Pilot pilot, Aircraft aircraft) {
        if (pilot == null || aircraft == null || aircraft.getAircraftType() == null) {
            return false;
        }

        Set<String> licenses = pilot.getLicenses();
        if (licenses == null) {
            licenses = new java.util.HashSet<>();
            pilot.setLicenses(licenses);
        }

        String typeId = aircraft.getAircraftType().getId();
        String model = aircraft.getAircraftType().getModel();
        boolean hasExactLicense = licenses.contains(typeId) || licenses.contains(model);
        if (hasExactLicense) {
            return true;
        }

        if (pilot.getSkill() >= 70 || pilot.getExperience() >= 15) {
            licenses.add(typeId);
            if (model != null && !model.isBlank()) {
                licenses.add(model);
            }
            return true;
        }

        return false;
    }

    private boolean isPilotAtCorrectLocation(Pilot pilot, Route route) {
        if (pilot == null || route == null) {
            return false;
        }
        String current = pilot.getCurrentLocation();
        return current != null && current.equalsIgnoreCase(route.getOrigin().getCode());
    }

    private boolean hasConflict(FlightSchedule schedule, List<FlightSchedule> existingSchedules) {
        if (schedule == null || existingSchedules == null || existingSchedules.isEmpty()) {
            return false;
        }

        for (FlightSchedule existing : existingSchedules) {
            if (existing == null || existing.getDayOfWeek() != schedule.getDayOfWeek()) {
                continue;
            }
            if (existing.getId() != null && existing.getId().equals(schedule.getId())) {
                continue;
            }

            int proposedStart = toMinutes(schedule.getDepartureTime());
            int proposedEnd = toMinutes(schedule.getArrivalTime());
            int existingStart = toMinutes(existing.getDepartureTime());
            int existingEnd = toMinutes(existing.getArrivalTime());

            if (proposedStart < existingEnd && proposedEnd > existingStart) {
                return true;
            }
        }

        return false;
    }

    private int toMinutes(String time) {
        if (time == null || !time.contains(":")) {
            return 0;
        }

        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}
