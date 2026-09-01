package com.skypilot.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pilots")
public class PilotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private int points;
    private int skill;
    private int experience;

    protected PilotEntity() {
    }

    public PilotEntity(String name) {
        this.name = name == null || name.isBlank() ? "Piloto" : name.trim();
        this.points = 0;
        this.skill = 40 + (int) (Math.random() * 60);
        this.experience = 10 + (int) (Math.random() * 90);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
