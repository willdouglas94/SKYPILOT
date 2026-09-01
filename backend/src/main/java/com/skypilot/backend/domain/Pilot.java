package com.skypilot.backend.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Pilot {
    private final String id;
    private final String name;
    private int points;
    private int skill;
    private int experience;
    private String currentLocation;
    private Set<String> licenses;

    public Pilot(String name) {
        this(UUID.randomUUID().toString(), name, 0, 40 + (int) (Math.random() * 60), 10 + (int) (Math.random() * 90), "GRU");
    }

    public Pilot(String id, String name, int points, int skill, int experience) {
        this(id, name, points, skill, experience, "GRU");
    }

    public Pilot(String id, String name, int points, int skill, int experience, String currentLocation) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.name = name == null || name.isBlank() ? "Piloto" : name.trim();
        this.points = points;
        this.skill = skill;
        this.experience = experience;
        this.currentLocation = currentLocation == null || currentLocation.isBlank() ? "GRU" : currentLocation.trim().toUpperCase();
        this.licenses = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation == null || currentLocation.isBlank() ? "GRU" : currentLocation.trim().toUpperCase();
    }

    public Set<String> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<String> licenses) {
        this.licenses = licenses == null ? new HashSet<>() : new HashSet<>(licenses);
    }
}
