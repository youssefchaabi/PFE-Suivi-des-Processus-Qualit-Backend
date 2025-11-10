package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.service.AiChartsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-charts")
@CrossOrigin(origins = "*")
public class AiChartsController {

    @Autowired
    private AiChartsService aiChartsService;

    // Récupérer les données de tendance
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrendData(@RequestParam(defaultValue = "8") int period) {
        try {
            Map<String, Object> trendData = aiChartsService.getTrendData(period);
            return ResponseEntity.ok(trendData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la récupération des données de tendance: " + e.getMessage()));
        }
    }

    // Récupérer les données de prédiction
    @GetMapping("/predictions")
    public ResponseEntity<Map<String, Object>> getPredictionData() {
        try {
            Map<String, Object> predictionData = aiChartsService.getPredictionData();
            return ResponseEntity.ok(predictionData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la récupération des données de prédiction: " + e.getMessage()));
        }
    }

    // Récupérer les données KPI
    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getKpiData() {
        try {
            Map<String, Object> kpiData = aiChartsService.getKpiData();
            return ResponseEntity.ok(kpiData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la récupération des données KPI: " + e.getMessage()));
        }
    }

    // Récupérer toutes les données du dashboard
    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> dashboardData = aiChartsService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la récupération des données du dashboard: " + e.getMessage()));
        }
    }

    // Récupérer les données de tendance réalistes
    @GetMapping("/real-trends")
    public ResponseEntity<Map<String, Object>> getRealTrendData() {
        try {
            Map<String, Object> realTrendData = aiChartsService.getRealTrendData();
            return ResponseEntity.ok(realTrendData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la récupération des données de tendance réalistes: " + e.getMessage()));
        }
    }

    // Récupérer les prédictions réalistes
    @GetMapping("/real-predictions")
    public ResponseEntity<Map<String, Object>> getRealPredictionData() {
        try {
            Map<String, Object> realPredictionData = aiChartsService.getRealPredictionData();
            return ResponseEntity.ok(realPredictionData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la récupération des prédictions réalistes: " + e.getMessage()));
        }
    }
} 