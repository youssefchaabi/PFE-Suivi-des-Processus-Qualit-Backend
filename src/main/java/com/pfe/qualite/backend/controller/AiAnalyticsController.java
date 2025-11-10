package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.service.AiAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-analytics")
@CrossOrigin(origins = "*")
public class AiAnalyticsController {

    @Autowired
    private AiAnalyticsService aiAnalyticsService;

    // Analyser les risques
    @GetMapping("/risques")
    public ResponseEntity<Map<String, Object>> analyserRisques() {
        Map<String, Object> response = new HashMap<>();
        response.put("predictions", aiAnalyticsService.analyserRisques());
        response.put("tauxConformite", calculerTauxConformite());
        response.put("fichesEnRetard", calculerFichesEnRetard());
        return ResponseEntity.ok(response);
    }

    // Générer des recommandations
    @GetMapping("/recommandations")
    public ResponseEntity<Map<String, Object>> genererRecommandations() {
        Map<String, Object> response = new HashMap<>();
        response.put("recommandations", aiAnalyticsService.genererRecommandations());
        response.put("totalFiches", calculerTotalFiches());
        response.put("tauxConformite", calculerTauxConformite());
        return ResponseEntity.ok(response);
    }

    // Analyser les tendances
    @GetMapping("/tendances")
    public ResponseEntity<Map<String, Object>> analyserTendances() {
        Map<String, Object> response = new HashMap<>();
        response.put("tendances", aiAnalyticsService.analyserTendances());
        response.put("totalFiches", calculerTotalFiches());
        response.put("tauxConformite", calculerTauxConformite());
        return ResponseEntity.ok(response);
    }

    // Optimiser les processus
    @GetMapping("/optimisations")
    public ResponseEntity<Map<String, Object>> optimiserProcessus() {
        Map<String, Object> response = new HashMap<>();
        response.put("optimisations", aiAnalyticsService.optimiserProcessus());
        response.put("efficaciteActuelle", calculerEfficaciteActuelle());
        return ResponseEntity.ok(response);
    }

    // Générer un rapport IA complet
    @GetMapping("/rapport")
    public ResponseEntity<Map<String, Object>> genererRapportIA() {
        return ResponseEntity.ok(aiAnalyticsService.genererRapportIA());
    }

    // Récupérer toutes les données du dashboard IA
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Risques
        Map<String, Object> risques = new HashMap<>();
        risques.put("predictions", aiAnalyticsService.analyserRisques());
        risques.put("tauxConformite", calculerTauxConformite());
        risques.put("fichesEnRetard", calculerFichesEnRetard());
        dashboardData.put("risques", risques);
        
        // Recommandations
        Map<String, Object> recommandations = new HashMap<>();
        recommandations.put("recommandations", aiAnalyticsService.genererRecommandations());
        recommandations.put("totalFiches", calculerTotalFiches());
        recommandations.put("tauxConformite", calculerTauxConformite());
        dashboardData.put("recommandations", recommandations);
        
        // Tendances
        Map<String, Object> tendances = new HashMap<>();
        tendances.put("tendances", aiAnalyticsService.analyserTendances());
        tendances.put("totalFiches", calculerTotalFiches());
        tendances.put("tauxConformite", calculerTauxConformite());
        dashboardData.put("tendances", tendances);
        
        // Optimisations
        Map<String, Object> optimisations = new HashMap<>();
        optimisations.put("optimisations", aiAnalyticsService.optimiserProcessus());
        optimisations.put("efficaciteActuelle", calculerEfficaciteActuelle());
        dashboardData.put("optimisations", optimisations);
        
        return ResponseEntity.ok(dashboardData);
    }

    // Méthodes utilitaires pour calculer les métriques
    private double calculerTauxConformite() {
        // Cette méthode devrait utiliser le repository pour calculer le taux de conformité
        // Pour l'instant, retournons une valeur par défaut
        return 75.0;
    }

    private int calculerFichesEnRetard() {
        // Cette méthode devrait utiliser le repository pour calculer les fiches en retard
        // Pour l'instant, retournons une valeur par défaut
        return 5;
    }

    private int calculerTotalFiches() {
        // Cette méthode devrait utiliser le repository pour calculer le total des fiches
        // Pour l'instant, retournons une valeur par défaut
        return 20;
    }

    private double calculerEfficaciteActuelle() {
        // Cette méthode devrait utiliser le repository pour calculer l'efficacité actuelle
        // Pour l'instant, retournons une valeur par défaut
        return 78.0;
    }
} 