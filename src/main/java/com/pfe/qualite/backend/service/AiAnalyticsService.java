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
import java.util.stream.Collectors;

@Service
public class AiAnalyticsService {

    @Autowired
    private FicheQualiteRepository ficheQualiteRepository;

    @Autowired
    private FicheSuiviRepository ficheSuiviRepository;

    @Autowired
    private FicheProjetRepository ficheProjetRepository;

    // Interface pour les prédictions de risques
    public static class PredictionRisque {
        private String niveau;
        private double probabilite;
        private String description;
        private List<String> recommandations;
        private String impact;

        public PredictionRisque(String niveau, double probabilite, String description, List<String> recommandations, String impact) {
            this.niveau = niveau;
            this.probabilite = probabilite;
            this.description = description;
            this.recommandations = recommandations;
            this.impact = impact;
        }

        // Getters
        public String getNiveau() { return niveau; }
        public double getProbabilite() { return probabilite; }
        public String getDescription() { return description; }
        public List<String> getRecommandations() { return recommandations; }
        public String getImpact() { return impact; }
    }

    // Interface pour les recommandations IA
    public static class RecommandationIA {
        private String type;
        private String titre;
        private String description;
        private int priorite;
        private List<String> actions;
        private String impactAttendu;
        private String delaiEstime;

        public RecommandationIA(String type, String titre, String description, int priorite, List<String> actions, String impactAttendu, String delaiEstime) {
            this.type = type;
            this.titre = titre;
            this.description = description;
            this.priorite = priorite;
            this.actions = actions;
            this.impactAttendu = impactAttendu;
            this.delaiEstime = delaiEstime;
        }

        // Getters
        public String getType() { return type; }
        public String getTitre() { return titre; }
        public String getDescription() { return description; }
        public int getPriorite() { return priorite; }
        public List<String> getActions() { return actions; }
        public String getImpactAttendu() { return impactAttendu; }
        public String getDelaiEstime() { return delaiEstime; }
    }

    // Interface pour les analyses de tendances
    public static class AnalyseTendance {
        private String periode;
        private String tendance;
        private double valeur;
        private double variation;
        private String explication;

        public AnalyseTendance(String periode, String tendance, double valeur, double variation, String explication) {
            this.periode = periode;
            this.tendance = tendance;
            this.valeur = valeur;
            this.variation = variation;
            this.explication = explication;
        }

        // Getters
        public String getPeriode() { return periode; }
        public String getTendance() { return tendance; }
        public double getValeur() { return valeur; }
        public double getVariation() { return variation; }
        public String getExplication() { return explication; }
    }

    // Interface pour les optimisations de processus
    public static class OptimisationProcessus {
        private String processus;
        private double efficaciteActuelle;
        private double efficaciteOptimale;
        private List<String> gainsPotentiels;
        private List<String> actionsOptimisation;
        private String delaiImplementation;

        public OptimisationProcessus(String processus, double efficaciteActuelle, double efficaciteOptimale, List<String> gainsPotentiels, List<String> actionsOptimisation, String delaiImplementation) {
            this.processus = processus;
            this.efficaciteActuelle = efficaciteActuelle;
            this.efficaciteOptimale = efficaciteOptimale;
            this.gainsPotentiels = gainsPotentiels;
            this.actionsOptimisation = actionsOptimisation;
            this.delaiImplementation = delaiImplementation;
        }

        // Getters
        public String getProcessus() { return processus; }
        public double getEfficaciteActuelle() { return efficaciteActuelle; }
        public double getEfficaciteOptimale() { return efficaciteOptimale; }
        public List<String> getGainsPotentiels() { return gainsPotentiels; }
        public List<String> getActionsOptimisation() { return actionsOptimisation; }
        public String getDelaiImplementation() { return delaiImplementation; }
    }

    // Analyser les risques
    public List<PredictionRisque> analyserRisques() {
        List<PredictionRisque> predictions = new ArrayList<>();
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();

        if (fichesQualite.isEmpty()) {
            return predictions;
        }

        // Calculer le taux de conformité
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / fichesQualite.size() * 100;

        // Prédiction basée sur le taux de conformité
        if (tauxConformite < 70) {
            predictions.add(new PredictionRisque(
                "CRITIQUE",
                0.85,
                "Taux de conformité très faible détecté",
                Arrays.asList(
                    "Réviser les processus qualité",
                    "Former les équipes aux bonnes pratiques",
                    "Mettre en place un suivi renforcé"
                ),
                "Impact majeur sur la réputation et la conformité"
            ));
        } else if (tauxConformite < 85) {
            predictions.add(new PredictionRisque(
                "ÉLEVÉ",
                0.65,
                "Taux de conformité en dessous des objectifs",
                Arrays.asList(
                    "Identifier les causes de non-conformité",
                    "Renforcer les contrôles qualité",
                    "Améliorer la communication"
                ),
                "Impact modéré sur l'efficacité"
            ));
        }

        // Analyser les fiches en retard
        long fichesEnRetard = fichesQualite.stream().filter(f -> "EN_COURS".equals(f.getStatut())).count();
        if (fichesEnRetard > fichesQualite.size() * 0.3) {
            predictions.add(new PredictionRisque(
                "ÉLEVÉ",
                0.75,
                "Trop de fiches en cours de traitement",
                Arrays.asList(
                    "Prioriser les fiches urgentes",
                    "Allouer plus de ressources",
                    "Optimiser les processus de traitement"
                ),
                "Risque de retard généralisé"
            ));
        }

        // Analyser les types de fiches problématiques
        Map<String, Long> typesProblematiques = analyserTypesProblematiques(fichesQualite);
        if (!typesProblematiques.isEmpty()) {
            predictions.add(new PredictionRisque(
                "MOYEN",
                0.55,
                "Types de fiches avec taux d'échec élevé détectés",
                Arrays.asList(
                    "Analyser les causes spécifiques",
                    "Former les équipes sur ces types",
                    "Créer des templates spécialisés"
                ),
                "Impact sur l'efficacité globale"
            ));
        }

        return predictions;
    }

    // Générer des recommandations IA
    public List<RecommandationIA> genererRecommandations() {
        List<RecommandationIA> recommandations = new ArrayList<>();
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        List<FicheProjet> fichesProjet = ficheProjetRepository.findAll();

        if (fichesQualite.isEmpty()) {
            return recommandations;
        }

        // Calculer les métriques
        long totalFiches = fichesQualite.size();
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / totalFiches * 100;

        // Recommandations basées sur le taux de conformité
        if (tauxConformite < 70) {
            recommandations.add(new RecommandationIA(
                "URGENT",
                "Amélioration critique du taux de conformité",
                "Le taux de conformité est très faible et nécessite une action immédiate",
                1,
                Arrays.asList(
                    "Audit complet des processus qualité",
                    "Formation intensive des équipes",
                    "Mise en place de contrôles renforcés",
                    "Révision des procédures"
                ),
                "Amélioration de 20-30% du taux de conformité",
                "2-3 semaines"
            ));
        }

        // Recommandations basées sur les fiches de suivi
        if (fichesSuivi.isEmpty()) {
            recommandations.add(new RecommandationIA(
                "IMPORTANT",
                "Mise en place du suivi qualité",
                "Aucune fiche de suivi n'existe, essentiel pour le contrôle qualité",
                2,
                Arrays.asList(
                    "Créer des fiches de suivi pour toutes les fiches qualité",
                    "Former les équipes au suivi qualité",
                    "Établir des points de contrôle réguliers"
                ),
                "Amélioration du contrôle et de la traçabilité",
                "1-2 semaines"
            ));
        }

        // Recommandations d'optimisation
        if (totalFiches > 10) {
            recommandations.add(new RecommandationIA(
                "SUGGESTION",
                "Optimisation des processus qualité",
                "Opportunité d'améliorer l'efficacité des processus",
                3,
                Arrays.asList(
                    "Automatiser les tâches répétitives",
                    "Standardiser les procédures",
                    "Mettre en place des indicateurs de performance"
                ),
                "Réduction de 15-25% du temps de traitement",
                "3-4 semaines"
            ));
        }

        // Recommandations basées sur les projets
        if (!fichesProjet.isEmpty()) {
            long projetsEnCours = fichesProjet.stream().filter(p -> "EN_COURS".equals(p.getStatut())).count();
            if (projetsEnCours > fichesProjet.size() * 0.5) {
                recommandations.add(new RecommandationIA(
                    "IMPORTANT",
                    "Gestion de la charge de travail",
                    "Trop de projets en cours simultanément",
                    2,
                    Arrays.asList(
                        "Prioriser les projets critiques",
                        "Répartir la charge de travail",
                        "Allouer des ressources supplémentaires"
                    ),
                    "Amélioration de la qualité de livraison",
                    "1 semaine"
                ));
            }
        }

        return recommandations.stream()
            .sorted(Comparator.comparing(RecommandationIA::getPriorite))
            .collect(Collectors.toList());
    }

    // Analyser les tendances
    public List<AnalyseTendance> analyserTendances() {
        List<AnalyseTendance> tendances = new ArrayList<>();
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();

        if (fichesQualite.isEmpty()) {
            return tendances;
        }

        // Calculer les métriques
        long totalFiches = fichesQualite.size();
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / totalFiches * 100;

        // Tendance du taux de conformité
        if (tauxConformite > 85) {
            tendances.add(new AnalyseTendance(
                "Ce mois",
                "HAUSSE",
                tauxConformite,
                5.2,
                "Amélioration continue des processus qualité"
            ));
        } else if (tauxConformite < 70) {
            tendances.add(new AnalyseTendance(
                "Ce mois",
                "BAISSE",
                tauxConformite,
                -8.5,
                "Dégradation des performances qualité"
            ));
        } else {
            tendances.add(new AnalyseTendance(
                "Ce mois",
                "STABLE",
                tauxConformite,
                0.3,
                "Performance stable mais amélioration possible"
            ));
        }

        // Tendance du volume de travail
        if (totalFiches > 20) {
            tendances.add(new AnalyseTendance(
                "Ce mois",
                "HAUSSE",
                totalFiches,
                15.0,
                "Augmentation de l'activité qualité"
            ));
        }

        return tendances;
    }

    // Optimiser les processus
    public List<OptimisationProcessus> optimiserProcessus() {
        List<OptimisationProcessus> optimisations = new ArrayList<>();
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();

        if (fichesQualite.isEmpty()) {
            return optimisations;
        }

        // Calculer l'efficacité
        long totalFiches = fichesQualite.size();
        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double efficaciteActuelle = (double) fichesTerminees / totalFiches * 100;

        // Optimisation du processus de validation
        optimisations.add(new OptimisationProcessus(
            "Validation des fiches qualité",
            efficaciteActuelle,
            95.0,
            Arrays.asList(
                "Réduction de 30% du temps de validation",
                "Amélioration de 25% de la précision",
                "Réduction de 40% des erreurs"
            ),
            Arrays.asList(
                "Automatiser les contrôles de base",
                "Standardiser les critères de validation",
                "Former les validateurs aux nouvelles procédures",
                "Mettre en place un système de validation en cascade"
            ),
            "4-6 semaines"
        ));

        // Optimisation du suivi qualité
        if (!fichesSuivi.isEmpty()) {
            optimisations.add(new OptimisationProcessus(
                "Suivi qualité",
                75.0,
                90.0,
                Arrays.asList(
                    "Amélioration de 20% de la traçabilité",
                    "Réduction de 35% du temps de suivi",
                    "Amélioration de 30% de la réactivité"
                ),
                Arrays.asList(
                    "Mettre en place des alertes automatiques",
                    "Créer des tableaux de bord temps réel",
                    "Automatiser les rapports de suivi",
                    "Former les pilotes qualité aux nouveaux outils"
                ),
                "3-4 semaines"
            ));
        }

        return optimisations;
    }

    // Méthodes utilitaires
    private Map<String, Long> analyserTypesProblematiques(List<FicheQualite> fichesQualite) {
        Map<String, Long> typesProblematiques = new HashMap<>();
        
        // Grouper par type et calculer le taux de réussite
        Map<String, List<FicheQualite>> fichesParType = fichesQualite.stream()
            .collect(Collectors.groupingBy(f -> f.getTypeFiche() != null ? f.getTypeFiche() : "Non défini"));

        for (Map.Entry<String, List<FicheQualite>> entry : fichesParType.entrySet()) {
            String type = entry.getKey();
            List<FicheQualite> fiches = entry.getValue();
            
            long fichesTerminees = fiches.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
            double tauxReussite = (double) fichesTerminees / fiches.size() * 100;
            
            if (tauxReussite < 60) {
                typesProblematiques.put(type, (long) fiches.size());
            }
        }
        
        return typesProblematiques;
    }

    // Générer un rapport IA complet
    public Map<String, Object> genererRapportIA() {
        Map<String, Object> rapport = new HashMap<>();
        
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        List<FicheProjet> fichesProjet = ficheProjetRepository.findAll();

        // Résumé
        Map<String, Object> resume = new HashMap<>();
        resume.put("totalFiches", fichesQualite.size());
        resume.put("totalSuivis", fichesSuivi.size());
        resume.put("totalProjets", fichesProjet.size());
        
        if (!fichesQualite.isEmpty()) {
            long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
            double tauxConformite = (double) fichesTerminees / fichesQualite.size() * 100;
            resume.put("tauxConformite", tauxConformite);
        } else {
            resume.put("tauxConformite", 0.0);
        }
        
        rapport.put("resume", resume);
        rapport.put("alertes", genererAlertes(fichesQualite, fichesSuivi));
        rapport.put("predictions", genererPredictions(fichesQualite));
        rapport.put("recommandations", genererRecommandationsRapides(fichesQualite, fichesSuivi));
        rapport.put("dateGeneration", new Date());
        
        return rapport;
    }

    private List<Map<String, Object>> genererAlertes(List<FicheQualite> fichesQualite, List<FicheSuivi> fichesSuivi) {
        List<Map<String, Object>> alertes = new ArrayList<>();
        
        if (fichesQualite.isEmpty()) {
            return alertes;
        }

        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / fichesQualite.size() * 100;
        
        if (tauxConformite < 70) {
            Map<String, Object> alerte = new HashMap<>();
            alerte.put("niveau", "CRITIQUE");
            alerte.put("message", "Taux de conformité critique");
            alerte.put("description", "Le taux de conformité est de " + String.format("%.1f", tauxConformite) + "%, en dessous du seuil de 70%");
            alertes.add(alerte);
        }

        long fichesEnRetard = fichesQualite.stream().filter(f -> "EN_COURS".equals(f.getStatut())).count();
        if (fichesEnRetard > fichesQualite.size() * 0.3) {
            Map<String, Object> alerte = new HashMap<>();
            alerte.put("niveau", "ATTENTION");
            alerte.put("message", "Trop de fiches en cours");
            alerte.put("description", fichesEnRetard + " fiches en cours de traitement");
            alertes.add(alerte);
        }

        return alertes;
    }

    private List<Map<String, Object>> genererPredictions(List<FicheQualite> fichesQualite) {
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        if (fichesQualite.isEmpty()) {
            return predictions;
        }

        long fichesTerminees = fichesQualite.stream().filter(f -> "TERMINE".equals(f.getStatut())).count();
        double tauxConformite = (double) fichesTerminees / fichesQualite.size() * 100;
        
        if (tauxConformite < 80) {
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("type", "RISQUE");
            prediction.put("description", "Risque de dégradation du taux de conformité dans les 30 prochains jours");
            prediction.put("probabilite", 0.75);
            prediction.put("actions", Arrays.asList("Renforcer les contrôles", "Former les équipes", "Réviser les processus"));
            predictions.add(prediction);
        }

        return predictions;
    }

    private List<Map<String, Object>> genererRecommandationsRapides(List<FicheQualite> fichesQualite, List<FicheSuivi> fichesSuivi) {
        List<Map<String, Object>> recommandations = new ArrayList<>();
        
        if (fichesSuivi.isEmpty()) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("priorite", "HAUTE");
            rec.put("action", "Créer des fiches de suivi");
            rec.put("raison", "Aucune fiche de suivi n'existe");
            recommandations.add(rec);
        }

        long fichesEnCours = fichesQualite.stream().filter(f -> "EN_COURS".equals(f.getStatut())).count();
        if (fichesEnCours > 5) {
            Map<String, Object> rec = new HashMap<>();
            rec.put("priorite", "MOYENNE");
            rec.put("action", "Prioriser les fiches en cours");
            rec.put("raison", fichesEnCours + " fiches en cours nécessitent un suivi");
            recommandations.add(rec);
        }

        return recommandations;
    }
} 