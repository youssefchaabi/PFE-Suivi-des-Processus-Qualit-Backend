package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.model.FicheSuivi;
import com.pfe.qualite.backend.model.FicheProjet;
import com.pfe.qualite.backend.model.FormulaireObligatoire;
import com.pfe.qualite.backend.repository.FicheQualiteRepository;
import com.pfe.qualite.backend.repository.FicheSuiviRepository;
import com.pfe.qualite.backend.repository.FicheProjetRepository;
import com.pfe.qualite.backend.repository.FormulaireObligatoireRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service désactivé temporairement - Sprint 2
 * Les modèles FicheQualite, FicheSuivi, FicheProjet seront créés au Sprint 2
 */
// @Service  // Désactivé temporairement
public class RapportKpiService {

    @Autowired
    private FicheQualiteRepository ficheQualiteRepository;

    @Autowired
    private FicheSuiviRepository ficheSuiviRepository;

    @Autowired
    private FicheProjetRepository ficheProjetRepository;

    @Autowired
    private FormulaireObligatoireRepository formulaireObligatoireRepository;

    /**
     * Générer un rapport KPI complet
     */
    public Map<String, Object> genererRapportKpiComplet() {
        Map<String, Object> rapport = new HashMap<>();
        
        // Statistiques générales
        rapport.put("statistiquesGenerales", getStatistiquesGenerales());
        
        // Statistiques par statut
        rapport.put("statistiquesParStatut", getStatistiquesParStatut());
        
        // Statistiques par type
        rapport.put("statistiquesParType", getStatistiquesParType());
        
        // Évolution temporelle
        rapport.put("evolutionTemporelle", getEvolutionTemporelle());
        
        // Formulaires obligatoires
        rapport.put("formulairesObligatoires", getStatistiquesFormulairesObligatoires());
        
        // Top des projets
        rapport.put("topProjets", getTopProjets());
        
        // Métriques de performance
        rapport.put("metriquesPerformance", getMetriquesPerformance());
        
        // Date de génération
        rapport.put("dateGeneration", new Date());
        
        return rapport;
    }

    /**
     * Statistiques générales
     */
    private Map<String, Object> getStatistiquesGenerales() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalFichesQualite = ficheQualiteRepository.count();
        long totalFichesSuivi = ficheSuiviRepository.count();
        long totalProjets = ficheProjetRepository.count();
        long totalFormulairesObligatoires = formulaireObligatoireRepository.count();
        
        stats.put("totalFichesQualite", totalFichesQualite);
        stats.put("totalFichesSuivi", totalFichesSuivi);
        stats.put("totalProjets", totalProjets);
        stats.put("totalFormulairesObligatoires", totalFormulairesObligatoires);
        stats.put("totalElements", totalFichesQualite + totalFichesSuivi + totalProjets);
        
        return stats;
    }

    /**
     * Statistiques par statut
     */
    private Map<String, Object> getStatistiquesParStatut() {
        Map<String, Object> stats = new HashMap<>();
        
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        Map<String, Long> statutsQualite = new HashMap<>();
        Map<String, Long> statutsSuivi = new HashMap<>();
        
        // Compter par statut pour les fiches qualité
        for (FicheQualite fiche : fichesQualite) {
            String statut = fiche.getStatut() != null ? fiche.getStatut() : "NON_DEFINI";
            statutsQualite.put(statut, statutsQualite.getOrDefault(statut, 0L) + 1);
        }
        
        // Compter par statut pour les fiches de suivi
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        for (FicheSuivi fiche : fichesSuivi) {
            String statut = fiche.getEtatAvancement() != null ? fiche.getEtatAvancement() : "NON_DEFINI";
            statutsSuivi.put(statut, statutsSuivi.getOrDefault(statut, 0L) + 1);
        }
        
        stats.put("statutsQualite", statutsQualite);
        stats.put("statutsSuivi", statutsSuivi);
        
        return stats;
    }

    /**
     * Statistiques par type
     */
    private Map<String, Object> getStatistiquesParType() {
        Map<String, Object> stats = new HashMap<>();
        
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        Map<String, Long> typesFiche = new HashMap<>();
        
        for (FicheQualite fiche : fichesQualite) {
            String type = fiche.getTypeFiche() != null ? fiche.getTypeFiche() : "NON_DEFINI";
            typesFiche.put(type, typesFiche.getOrDefault(type, 0L) + 1);
        }
        
        stats.put("typesFiche", typesFiche);
        
        return stats;
    }

    /**
     * Évolution temporelle (agrégation par mois sur les 6 derniers mois)
     * Utilise FicheSuivi.dateSuivi comme source temporelle.
     */
    private Map<String, Object> getEvolutionTemporelle() {
        Map<String, Object> evolution = new HashMap<>();

        // Fenêtre: 6 derniers mois (mois en cours inclus)
        Calendar cal = Calendar.getInstance();
        Date dateFin = cal.getTime();

        // Normaliser au début du mois courant
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Construire les 6 mois (ordre chronologique)
        List<Date> mois = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.MONTH, -i);
            mois.add(c.getTime());
        }
        Date dateDebut = mois.get(0);

        // Préparer le compteur par libellé de mois
        Map<String, Long> fichesParMois = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        for (Date m : mois) {
            String label = formatMonthLabel(m);
            labels.add(label);
            fichesParMois.put(label, 0L);
        }

        // Charger les suivis et compter par mois
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        for (FicheSuivi fs : fichesSuivi) {
            Date d = fs.getDateSuivi();
            if (d == null) continue;
            // Si hors période, ignorer
            if (d.before(dateDebut) || d.after(dateFin)) continue;

            // Trouver le mois correspondant
            String label = formatMonthLabel(truncateToMonth(d));
            if (fichesParMois.containsKey(label)) {
                fichesParMois.put(label, fichesParMois.get(label) + 1);
            }
        }

        // Compatibilité: exposer aussi "fichesParJour" avec les mêmes données
        evolution.put("fichesParMois", fichesParMois);
        evolution.put("fichesParJour", fichesParMois);
        evolution.put("labels", labels);
        evolution.put("periode", Map.of("debut", dateDebut, "fin", dateFin));

        return evolution;
    }

    private Date truncateToMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private String formatMonthLabel(Date date) {
        // Libellés courts FR normalisés pour correspondre à l'affichage
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY: return "Jan";
            case Calendar.FEBRUARY: return "Fév";
            case Calendar.MARCH: return "Mar";
            case Calendar.APRIL: return "Avr";
            case Calendar.MAY: return "Mai";
            case Calendar.JUNE: return "Juin";
            case Calendar.JULY: return "Juil";
            case Calendar.AUGUST: return "Août";
            case Calendar.SEPTEMBER: return "Sep";
            case Calendar.OCTOBER: return "Oct";
            case Calendar.NOVEMBER: return "Nov";
            case Calendar.DECEMBER: return "Déc";
            default: return new SimpleDateFormat("MMM").format(date);
        }
    }

    /**
     * Statistiques des formulaires obligatoires
     */
    private Map<String, Object> getStatistiquesFormulairesObligatoires() {
        Map<String, Object> stats = new HashMap<>();
        
        List<FormulaireObligatoire> formulaires = formulaireObligatoireRepository.findAll();
        
        long enAttente = 0, soumis = 0, enRetard = 0, annule = 0;
        long haute = 0, moyenne = 0, basse = 0;
        
        for (FormulaireObligatoire formulaire : formulaires) {
            // Compter par statut
            switch (formulaire.getStatut()) {
                case "EN_ATTENTE": enAttente++; break;
                case "SOUMIS": soumis++; break;
                case "EN_RETARD": enRetard++; break;
                case "ANNULE": annule++; break;
            }
            
            // Compter par priorité
            switch (formulaire.getPriorite()) {
                case "HAUTE": haute++; break;
                case "MOYENNE": moyenne++; break;
                case "BASSE": basse++; break;
            }
        }
        
        stats.put("parStatut", Map.of(
            "EN_ATTENTE", enAttente,
            "SOUMIS", soumis,
            "EN_RETARD", enRetard,
            "ANNULE", annule
        ));
        
        stats.put("parPriorite", Map.of(
            "HAUTE", haute,
            "MOYENNE", moyenne,
            "BASSE", basse
        ));
        
        stats.put("total", formulaires.size());
        
        return stats;
    }

    /**
     * Top des projets
     */
    private Map<String, Object> getTopProjets() {
        Map<String, Object> top = new HashMap<>();
        
        List<FicheProjet> projets = ficheProjetRepository.findAll();
        
        // Trier par date d'échéance (plus proche en premier)
        projets.sort((p1, p2) -> {
            if (p1.getEcheance() == null && p2.getEcheance() == null) return 0;
            if (p1.getEcheance() == null) return 1;
            if (p2.getEcheance() == null) return -1;
            return p1.getEcheance().compareTo(p2.getEcheance());
        });
        
        // Prendre les 10 premiers
        List<FicheProjet> top10 = projets.size() > 10 ? projets.subList(0, 10) : projets;
        
        List<Map<String, Object>> projetsFormates = new ArrayList<>();
        for (FicheProjet projet : top10) {
            Map<String, Object> projetFormate = new HashMap<>();
            projetFormate.put("id", projet.getId());
            projetFormate.put("nom", projet.getNom());
            projetFormate.put("echeance", projet.getEcheance());
            projetFormate.put("statut", projet.getStatut());
            projetsFormates.add(projetFormate);
        }
        
        top.put("projets", projetsFormates);
        
        return top;
    }

    /**
     * Métriques de performance
     */
    private Map<String, Object> getMetriquesPerformance() {
        Map<String, Object> metriques = new HashMap<>();
        
        List<FormulaireObligatoire> formulaires = formulaireObligatoireRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        
        // Taux de conformité basé sur FicheSuivi.indicateursKpi
        long totalEvalues = 0;
        long nbConformes = 0;
        for (FicheSuivi suivi : fichesSuivi) {
            Double taux = null;
            if (suivi.getTauxConformite() != null) {
                taux = suivi.getTauxConformite();
            } else if (suivi.getIndicateursKpi() != null) {
                taux = extraireTauxConformite(suivi.getIndicateursKpi().toLowerCase());
            }
            if (taux == null) continue;
            totalEvalues++;
            if (taux >= 80.0) nbConformes++;
        }
        double tauxConformite = totalEvalues > 0 ? (double) nbConformes / totalEvalues * 100 : 0;
        
        // Taux de soumission des formulaires obligatoires
        long formulairesSoumis = formulaires.stream()
            .filter(f -> "SOUMIS".equals(f.getStatut()))
            .count();
        double tauxSoumission = formulaires.size() > 0 ? 
            (double) formulairesSoumis / formulaires.size() * 100 : 0;
        
        // Taux de retard
        long formulairesEnRetard = formulaires.stream()
            .filter(f -> "EN_RETARD".equals(f.getStatut()))
            .count();
        double tauxRetard = formulaires.size() > 0 ? 
            (double) formulairesEnRetard / formulaires.size() * 100 : 0;
        
        metriques.put("tauxConformite", Math.round(tauxConformite * 100.0) / 100.0);
        metriques.put("evaluationsConformite", totalEvalues);
        metriques.put("nbConformes", nbConformes);
        metriques.put("tauxSoumission", Math.round(tauxSoumission * 100.0) / 100.0);
        metriques.put("tauxRetard", Math.round(tauxRetard * 100.0) / 100.0);
        metriques.put("formulairesSoumis", formulairesSoumis);
        metriques.put("formulairesEnRetard", formulairesEnRetard);
        
        return metriques;
    }

    private Double extraireTauxConformite(String text) {
        if (text == null) return null;
        String t = text.trim();
        // a) JSON key-like: "tauxConformite": 85 or 85%
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("taux\\s*[_-]?\\s*conform(?:ite|ité)\\s*\"?\\s*:\\s*(\\d{1,3})\\s*%?")
            .matcher(t);
        if (m.find()) {
            try {
                int v = Integer.parseInt(m.group(1));
                return (double) Math.max(0, Math.min(100, v));
            } catch (NumberFormatException ignored) { }
        }
        // b) Label + % : "taux de conformité ... 85%"
        m = java.util.regex.Pattern
            .compile("taux\\s*(?:de)?\\s*conform(?:ite|ité)[^\\d%]{0,30}(\\d{1,3})\\s*%")
            .matcher(t);
        if (m.find()) {
            try {
                int v = Integer.parseInt(m.group(1));
                return (double) Math.max(0, Math.min(100, v));
            } catch (NumberFormatException ignored) { }
        }
        // c) Label + décimal : "taux de conformité ... 0.85"
        m = java.util.regex.Pattern
            .compile("taux\\s*(?:de)?\\s*conform(?:ite|ité)[^\\d.]{0,30}(0?\\.\\d+|1(?:\\.0+)?)\\b")
            .matcher(t);
        if (m.find()) {
            try {
                double v = Double.parseDouble(m.group(1));
                double pct = v * 100.0;
                return Math.max(0.0, Math.min(100.0, pct));
            } catch (NumberFormatException ignored) { }
        }
        return null;
    }

    /**
     * Générer un rapport personnalisé par période
     */
    public Map<String, Object> genererRapportParPeriode(Date dateDebut, Date dateFin) {
        Map<String, Object> rapport = new HashMap<>();
        
        // Filtrer les données par période
        List<FicheQualite> fichesQualite = ficheQualiteRepository.findAll();
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        List<FormulaireObligatoire> formulaires = formulaireObligatoireRepository.findAll();
        
        // Pour l'instant, on utilise toutes les données car les modèles n'ont pas de date de création
        List<FicheQualite> fichesQualiteFiltrees = fichesQualite;
        List<FormulaireObligatoire> formulairesFiltres = formulaires;
        
        rapport.put("periode", Map.of("debut", dateDebut, "fin", dateFin));
        rapport.put("fichesQualite", fichesQualiteFiltrees.size());
        rapport.put("fichesSuivi", fichesSuivi.size());
        rapport.put("formulairesObligatoires", formulairesFiltres.size());
        
        return rapport;
    }
} 