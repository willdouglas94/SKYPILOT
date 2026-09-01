package com.skypilot.backend.domain;

public class AircraftType {
    private final String id;
    private final String manufacturer;
    private final String model;
    private final String category;
    private final int rangeKm;
    private final int capacity;

    public AircraftType(String id, String manufacturer, String model, String category, int rangeKm, int capacity) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.manufacturer = manufacturer == null || manufacturer.isBlank() ? "Airbus" : manufacturer.trim();
        this.model = model == null || model.isBlank() ? "A320" : model.trim();
        this.category = category == null || category.isBlank() ? "NARROWBODY" : category.trim().toUpperCase();
        this.rangeKm = rangeKm > 0 ? rangeKm : 2500;
        this.capacity = capacity > 0 ? capacity : 180;
    }

    public String getId() { return id; }
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public String getCategory() { return category; }
    public int getRangeKm() { return rangeKm; }
    public int getCapacity() { return capacity; }
}
