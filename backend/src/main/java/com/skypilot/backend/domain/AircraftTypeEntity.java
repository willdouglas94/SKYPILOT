package com.skypilot.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aircraft_types")
public class AircraftTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 255)
    private String manufacturer;

    @Column(nullable = false, length = 255)
    private String model;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private int rangeKm;

    @Column(nullable = false)
    private int capacity;

    protected AircraftTypeEntity() {
    }

    public AircraftTypeEntity(String manufacturer, String model, String category, int rangeKm, int capacity) {
        this.manufacturer = manufacturer == null || manufacturer.isBlank() ? "UNKNOWN" : manufacturer.trim();
        this.model = model == null || model.isBlank() ? "UNKNOWN" : model.trim();
        this.category = category == null || category.isBlank() ? "UNKNOWN" : category.trim().toUpperCase();
        this.rangeKm = rangeKm > 0 ? rangeKm : 1500;
        this.capacity = capacity > 0 ? capacity : 0;
    }

    public String getId() {
        return id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public int getRangeKm() {
        return rangeKm;
    }

    public int getCapacity() {
        return capacity;
    }
}
