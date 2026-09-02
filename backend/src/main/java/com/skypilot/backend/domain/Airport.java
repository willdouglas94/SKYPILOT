package com.skypilot.backend.domain;

import java.time.Instant;

public class Airport {
    private final String code;
    private final String city;
    private final String country;
    private final String source;
    private final String externalId;
    private final Instant sourceUpdatedAt;
    private final Instant lastSyncedAt;

    public Airport(String code, String city, String country) {
        this(code, city, country, "USER_CREATED", null, null, null);
    }

    public Airport(String code, String city, String country, String source, String externalId,
                   Instant sourceUpdatedAt, Instant lastSyncedAt) {
        this.code = normalizeCode(code);
        this.city = normalizeText(city, "UNKNOWN");
        this.country = normalizeText(country, "UNKNOWN");
        this.source = normalizeText(source, "USER_CREATED");
        this.externalId = externalId == null || externalId.isBlank() ? null : externalId.trim();
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.lastSyncedAt = lastSyncedAt != null ? lastSyncedAt : Instant.now();
    }

    public String getCode() { return code; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getSource() { return source; }
    public String getExternalId() { return externalId; }
    public Instant getSourceUpdatedAt() { return sourceUpdatedAt; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
