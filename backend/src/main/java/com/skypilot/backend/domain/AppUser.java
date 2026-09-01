package com.skypilot.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    protected AppUser() {
    }

    public AppUser(String username, String password) {
        this(username, password, "USER");
    }

    public AppUser(String username, String password, String role) {
        this.username = username == null || username.isBlank() ? "pilot" : username.trim();
        this.password = password == null ? "" : password;
        this.role = normalizeRole(role);
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = normalizeRole(role);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        String normalized = role.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "DISPATCHER", "PILOT", "USER" -> normalized;
            default -> "USER";
        };
    }
}
