package com.skypilot.backend.service;

import com.skypilot.backend.domain.AppUser;
import com.skypilot.backend.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Map<String, String> register(String username, String password) {
        return register(username, password, "USER");
    }

    public Map<String, String> register(String username, String password, String role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username obrigatório");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Senha com mínimo de 8 caracteres");
        }

        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Usuário já existe");
        }

        AppUser user = new AppUser(username, passwordEncoder.encode(password), role);
        appUserRepository.save(user);
        return Map.of("message", "Usuário criado com sucesso");
    }

    public Map<String, String> login(String username, String password) {
        Optional<AppUser> optional = appUserRepository.findByUsername(username);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        AppUser user = optional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        return Map.of("token", jwtService.generateToken(user.getUsername(), user.getRole()));
    }
}
