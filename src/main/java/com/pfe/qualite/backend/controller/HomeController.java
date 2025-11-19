package com.pfe.qualite.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "API Suivi des Processus Qualité");
        response.put("version", "1.0.0");
        response.put("status", "✅ Running");
        response.put("message", "Backend déployé avec succès");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("login", "POST /api/auth/login");
        endpoints.put("forgot-password", "POST /api/auth/forgot-password");
        endpoints.put("reset-password", "POST /api/auth/reset-password");
        endpoints.put("utilisateurs", "GET /api/utilisateurs");
        
        response.put("endpoints", endpoints);
        
        return response;
    }
}
