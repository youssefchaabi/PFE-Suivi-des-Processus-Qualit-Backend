package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.model.FicheSuivi;
import com.pfe.qualite.backend.model.FicheProjet;
import com.pfe.qualite.backend.repository.FicheQualiteRepository;
import com.pfe.qualite.backend.repository.FicheSuiviRepository;
import com.pfe.qualite.backend.repository.FicheProjetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiChartsService {

    @Autowired
    private FicheQualiteRepository ficheQualiteRepository;

    @Autowired
    private FicheSuiviRepository ficheSuiviRepository;

    @Autowired
    private FicheProjetRepository ficheProjetRepository;

    // Générer les données de tendance
    public Map<String, Object> getTrendData(int period) {
        Map<String, Object> trendData = new HashMap<>();
        
        // Générer des données de tendance basées sur la période
        List<String> labels = generateMonthLabels(period);
        List<Double> conformityData = generateConformityData(period);
        List<Double> targetData = generateTargetData(period);
        
        Map<String, Object> dataset1 = new HashMap<>();
        dataset1.put("label", "Taux de Conformité (%)");
        dataset1.put("data", conformityData);
        dataset1.put("borderColor", "#ff6384");
        dataset1.put("backgroundColor", "rgba(255, 99, 132, 0.1)");
        dataset1.put("tension", 0.4);
        dataset1.put("fill", true);
        
        Map<String, Object> dataset2 = new HashMap<>();
        dataset2.put("label", "Objectif (%)");
        dataset2.put("data", targetData);
        dataset2.put("borderColor", "#36a2eb");
        dataset2.put("backgroundColor", "rgba(54, 162, 235, 0.1)");
        dataset2.put("borderDash", Arrays.asList(5, 5));
        dataset2.put("tension", 0.4);
        
        trendData.put("labels", labels);
        trendData.put("datasets", Arrays.asList(dataset1, dataset2));
        
        return trendData;
    }

    // Générer les données de prédiction
    public Map<String, Object> getPredictionData() {
        Map<String, Object> predictionData = new HashMap<>();
        
        List<String> labels = Arrays.asList("Contrôle", "Audit", "Amélioration", "Formation", "Maintenance");
        List<Double> data = Arrays.asList(85.0, 65.0, 45.0, 30.0, 20.0);
        
        List<String> backgroundColor = Arrays.asList(
            "rgba(255, 99, 132, 0.8)",
            "rgba(255, 159, 64, 0.8)",
            "rgba(255, 205, 86, 0.8)",
            "rgba(75, 192, 192, 0.8)",
            "rgba(54, 162, 235, 0.8)"
        );
        
        List<String> borderColor = Arrays.asList(
            "rgba(255, 99, 132, 1)",
            "rgba(255, 159, 64, 1)",
            "rgba(255, 205, 86, 1)",
            "rgba(75, 192, 192, 1)",
            "rgba(54, 162, 235, 1)"
        );
        
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "Risque Prédit (%)");
        dataset.put("data", data);
        dataset.put("backgroundColor", backgroundColor);
        dataset.put("borderColor", borderColor);
        dataset.put("borderWidth", 2);
        
        predictionData.put("labels", labels);
        predictionData.put("datasets", Arrays.asList(dataset));
        
        return predictionData;
    }

    // Générer les données KPI
    public Map<String, Object> getKpiData() {
        Map<String, Object> kpiData = new HashMap<>();
        
        List<String> labels = Arrays.asList("Conformité", "Efficacité", "Satisfaction", "Innovation");
        List<Double> data = Arrays.asList(65.0, 78.0, 85.0, 72.0);
        
        List<String> backgroundColor = Arrays.asList(
            "#ff6384",
            "#36a2eb",
            "#ffce56",
            "#4bc0c0"
        );
        
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "KPI Qualité");
        dataset.put("data", data);
        dataset.put("backgroundColor", backgroundColor);
        dataset.put("borderWidth", 2);
        dataset.put("borderColor", "#fff");
        
        kpiData.put("labels", labels);
        kpiData.put("datasets", Arrays.asList(dataset));
        
        return kpiData;
    }

    // Générer les données du dashboard complet
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        dashboardData.put("trends", getTrendData(8));
        dashboardData.put("predictions", getPredictionData());
        dashboardData.put("kpi", getKpiData());
        
        // Ajouter des métriques supplémentaires
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("scoreIA", calculateIAScore());
        metrics.put("confiance", calculateConfidence());
        metrics.put("alertes", calculateAlerts());
        metrics.put("optimisations", calculateOptimizations());
        
        dashboardData.put("metrics", metrics);
        
        return dashboardData;
    }

    // Générer des données de tendance réalistes
    public Map<String, Object> getRealTrendData() {
        Map<String, Object> trendData = new HashMap<>();
        
        // Utiliser les vraies données de la base
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        
        if (fichesQualite.isEmpty()) {
            return getTrendData(8); // Retourner des données mock si pas de données
        }
        
        // Calculer les vraies métriques
        long totalFiches = fichesQualite.size();
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / totalFiches * 100;
        
        // Générer des données basées sur le taux réel
        List<String> labels = Arrays.asList("Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août");
        List<Double> conformityData = generateRealisticConformityData(tauxConformite);
        List<Double> targetData = generateTargetData(8);
        
        Map<String, Object> dataset1 = new HashMap<>();
        dataset1.put("label", "Taux de Conformité (%)");
        dataset1.put("data", conformityData);
        dataset1.put("borderColor", "#ff6384");
        dataset1.put("backgroundColor", "rgba(255, 99, 132, 0.1)");
        dataset1.put("tension", 0.4);
        dataset1.put("fill", true);
        
        Map<String, Object> dataset2 = new HashMap<>();
        dataset2.put("label", "Objectif (%)");
        dataset2.put("data", targetData);
        dataset2.put("borderColor", "#36a2eb");
        dataset2.put("backgroundColor", "rgba(54, 162, 235, 0.1)");
        dataset2.put("borderDash", Arrays.asList(5, 5));
        dataset2.put("tension", 0.4);
        
        trendData.put("labels", labels);
        trendData.put("datasets", Arrays.asList(dataset1, dataset2));
        
        return trendData;
    }

    // Générer des prédictions réalistes
    public Map<String, Object> getRealPredictionData() {
        Map<String, Object> predictionData = new HashMap<>();
        
        // Analyser les vraies données pour générer des prédictions
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        
        if (fichesQualite.isEmpty()) {
            return getPredictionData(); // Retourner des données mock si pas de données
        }
        
        // Calculer les risques basés sur les vraies données
        Map<String, Double> riskLevels = calculateRealRiskLevels(fichesQualite);
        
        List<String> labels = new ArrayList<>(riskLevels.keySet());
        List<Double> data = new ArrayList<>(riskLevels.values());
        List<String> backgroundColor = generateColors(labels.size());
        List<String> borderColor = generateBorderColors(labels.size());
        
        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "Risque Prédit (%)");
        dataset.put("data", data);
        dataset.put("backgroundColor", backgroundColor);
        dataset.put("borderColor", borderColor);
        dataset.put("borderWidth", 2);
        
        predictionData.put("labels", labels);
        predictionData.put("datasets", Arrays.asList(dataset));
        
        return predictionData;
    }

    // Méthodes utilitaires
    private List<String> generateMonthLabels(int period) {
        List<String> months = Arrays.asList("Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc");
        return months.subList(0, Math.min(period, months.size()));
    }

    private List<Double> generateConformityData(int period) {
        List<Double> data = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < period; i++) {
            // Générer des données réalistes avec une tendance légèrement décroissante
            double baseValue = 85.0 - (i * 2.5) + (random.nextDouble() * 10 - 5);
            data.add(Math.max(60.0, Math.min(95.0, baseValue)));
        }
        
        return data;
    }

    private List<Double> generateRealisticConformityData(double currentRate) {
        List<Double> data = new ArrayList<>();
        Random random = new Random();
        
        // Générer des données basées sur le taux actuel avec une variation réaliste
        for (int i = 0; i < 8; i++) {
            double variation = (random.nextDouble() * 20 - 10); // Variation de ±10%
            double value = currentRate + variation;
            data.add(Math.max(50.0, Math.min(100.0, value)));
        }
        
        return data;
    }

    private List<Double> generateTargetData(int period) {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < period; i++) {
            data.add(90.0); // Objectif constant de 90%
        }
        return data;
    }

    private Map<String, Double> calculateRealRiskLevels(List<FicheQualite> fichesQualite) {
        Map<String, Double> riskLevels = new HashMap<>();
        
        // Analyser les types de fiches
        Map<String, Long> typesCount = fichesQualite.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                f -> f.getTypeFiche() != null ? f.getTypeFiche() : "Non défini",
                java.util.stream.Collectors.counting()
            ));
        
        // Calculer les risques basés sur les statuts
        Map<String, Long> statusCount = fichesQualite.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                f -> f.getStatut() != null ? f.getStatut() : "Non défini",
                java.util.stream.Collectors.counting()
            ));
        
        // Ajouter les types avec le plus de risques
        for (Map.Entry<String, Long> entry : typesCount.entrySet()) {
            if (entry.getValue() > 2) { // Seulement les types avec plus de 2 fiches
                double risk = Math.min(100.0, entry.getValue() * 15.0); // Risque basé sur le nombre
                riskLevels.put(entry.getKey(), risk);
            }
        }
        
        // Ajouter les statuts problématiques
        for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
            if ("EN_COURS".equals(entry.getKey()) || "BLOQUE".equals(entry.getKey())) {
                double risk = Math.min(100.0, entry.getValue() * 20.0);
                riskLevels.put(entry.getKey(), risk);
            }
        }
        
        return riskLevels;
    }

    private List<String> generateColors(int count) {
        List<String> colors = Arrays.asList(
            "rgba(255, 99, 132, 0.8)",
            "rgba(255, 159, 64, 0.8)",
            "rgba(255, 205, 86, 0.8)",
            "rgba(75, 192, 192, 0.8)",
            "rgba(54, 162, 235, 0.8)",
            "rgba(153, 102, 255, 0.8)",
            "rgba(255, 159, 64, 0.8)"
        );
        
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(colors.get(i % colors.size()));
        }
        return result;
    }

    private List<String> generateBorderColors(int count) {
        List<String> colors = Arrays.asList(
            "rgba(255, 99, 132, 1)",
            "rgba(255, 159, 64, 1)",
            "rgba(255, 205, 86, 1)",
            "rgba(75, 192, 192, 1)",
            "rgba(54, 162, 235, 1)",
            "rgba(153, 102, 255, 1)",
            "rgba(255, 159, 64, 1)"
        );
        
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(colors.get(i % colors.size()));
        }
        return result;
    }

    // Calculer le score IA basé sur les vraies données
    private double calculateIAScore() {
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        
        if (fichesQualite.isEmpty()) {
            return 75.0; // Score par défaut
        }
        
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / fichesQualite.size() * 100;
        
        // Score basé sur le taux de conformité et le nombre de fiches de suivi
        double score = tauxConformite + (fichesSuivi.size() * 2);
        return Math.min(100.0, Math.max(0.0, score));
    }

    // Calculer le niveau de confiance
    private double calculateConfidence() {
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        List<FicheProjet> fichesProjet = ficheProjetRepository.findAll();
        
        // Confiance basée sur la quantité de données
        double confidence = (fichesQualite.size() * 10) + (fichesSuivi.size() * 15) + (fichesProjet.size() * 10);
        return Math.min(100.0, Math.max(0.0, confidence));
    }

    // Calculer le nombre d'alertes
    private int calculateAlerts() {
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        
        if (fichesQualite.isEmpty()) {
            return 1; // Alerte par défaut
        }
        
        long fichesEnCours = fichesQualite.stream().filter(f -> "EN_COURS".equals(f.getStatut())).count();
        long fichesBloquees = fichesQualite.stream().filter(f -> "BLOQUE".equals(f.getStatut())).count();
        
        return (int) (fichesEnCours + fichesBloquees);
    }

    // Calculer le nombre d'optimisations
    private int calculateOptimizations() {
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        
        if (fichesQualite.isEmpty()) {
            return 2; // Optimisations par défaut
        }
        
        // Basé sur le nombre de fiches et leur statut
        long totalFiches = fichesQualite.size();
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / totalFiches * 100;
        
        if (tauxConformite < 70) {
            return 3; // Plus d'optimisations si le taux est faible
        } else if (tauxConformite < 85) {
            return 2;
        } else {
            return 1;
        }
    }
} 