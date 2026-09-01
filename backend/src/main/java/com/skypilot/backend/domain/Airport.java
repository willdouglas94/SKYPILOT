package com.skypilot.backend.domain;

public class Airport {
    private final String code;
    private final String city;
    private final String country;

    public Airport(String code, String city, String country) {
        this.code = code == null || code.isBlank() ? "GRU" : code.trim().toUpperCase();
        this.city = city == null || city.isBlank() ? "São Paulo" : city.trim();
        this.country = country == null || country.isBlank() ? "Brazil" : country.trim();
    }

    public String getCode() { return code; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
}
