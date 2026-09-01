package com.skypilot.backend.service;

import com.skypilot.backend.domain.FlightOffer;
import com.skypilot.backend.domain.Pilot;
import com.skypilot.backend.domain.PilotEntity;
import com.skypilot.backend.dto.RaceRequest;
import com.skypilot.backend.dto.RaceResult;
import com.skypilot.backend.repository.PilotRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PilotCareerService {

    private final PilotRepository pilotRepository;
    private final PilotPerformanceService pilotPerformanceService;

    public PilotCareerService(PilotRepository pilotRepository, PilotPerformanceService pilotPerformanceService) {
        this.pilotRepository = pilotRepository;
        this.pilotPerformanceService = pilotPerformanceService;
    }

    public Pilot createPilot(String name) {
        PilotEntity entity = new PilotEntity(name);
        PilotEntity saved = pilotRepository.save(entity);
        return mapToDomain(saved);
    }

    public List<Pilot> getPilots() {
        return pilotRepository.findAll().stream()
                .map(this::mapToDomain)
                .toList();
    }

    public void recordCompletedFlight(String pilotId, FlightOffer offer, int score, String notes) {
        if (pilotId == null || pilotId.isBlank()) {
            throw new IllegalArgumentException("Pilot id obrigatório");
        }

        pilotPerformanceService.recordCompletedFlight(pilotId, offer, score, notes);

        pilotRepository.findById(pilotId).ifPresent(entity -> {
            int pointsAward = Math.max(10, score / 5);
            int experienceAward = Math.max(2, score / 20);
            entity.setPoints(entity.getPoints() + pointsAward);
            entity.setExperience(entity.getExperience() + experienceAward);
            pilotRepository.save(entity);
        });
    }

    public List<Map<String, Object>> getRanking() {
        return pilotRepository.findAll().stream()
                .map(entity -> {
                    Map<String, Object> summary = pilotPerformanceService.getSummary(entity.getId());
                    int completedFlights = ((Number) summary.getOrDefault("completedFlights", 0)).intValue();
                    double averageScore = ((Number) summary.getOrDefault("averageScore", 0.0)).doubleValue();
                    int points = entity.getPoints();
                    int experience = entity.getExperience();
                    int reputation = points + (int) Math.round(averageScore * 2.0) + (completedFlights * 12) + (experience / 3);

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("pilotId", entity.getId());
                    row.put("name", entity.getName());
                    row.put("points", points);
                    row.put("experience", experience);
                    row.put("completedFlights", completedFlights);
                    row.put("averageScore", averageScore);
                    row.put("lastRouteCode", summary.getOrDefault("lastRouteCode", "N/A"));
                    row.put("reputation", reputation);
                    return row;
                })
                .sorted(Comparator.comparing((Map<String, Object> row) -> ((Number) row.get("reputation")).intValue()).reversed())
                .toList();
    }

    public RaceResult simulateRace(RaceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Requisição de corrida obrigatória");
        }

        PilotEntity pilotOne = pilotRepository.findById(request.pilotOneId())
                .orElseThrow(() -> new IllegalArgumentException("Piloto 1 não encontrado"));
        PilotEntity pilotTwo = pilotRepository.findById(request.pilotTwoId())
                .orElseThrow(() -> new IllegalArgumentException("Piloto 2 não encontrado"));

        int scoreOne = pilotOne.getSkill() + pilotOne.getExperience() + ThreadLocalRandom.current().nextInt(0, 40);
        int scoreTwo = pilotTwo.getSkill() + pilotTwo.getExperience() + ThreadLocalRandom.current().nextInt(0, 40);

        PilotEntity winner = scoreOne >= scoreTwo ? pilotOne : pilotTwo;
        PilotEntity loser = winner == pilotOne ? pilotTwo : pilotOne;

        int winnerPoints = 25;
        int loserPoints = 5;

        winner.setPoints(winner.getPoints() + winnerPoints);
        winner.setExperience(winner.getExperience() + 10);
        loser.setExperience(loser.getExperience() + 3);

        pilotRepository.save(winner);
        pilotRepository.save(loser);

        String summary = winner.getName() + " venceu a corrida com " + scoreOne + " x " + scoreTwo + ".";

        return new RaceResult(
                winner.getId(),
                winner.getName(),
                loser.getId(),
                loser.getName(),
                winnerPoints,
                loserPoints,
                summary
        );
    }

    private Pilot mapToDomain(PilotEntity entity) {
        return new Pilot(entity.getId(), entity.getName(), entity.getPoints(), entity.getSkill(), entity.getExperience());
    }
}
