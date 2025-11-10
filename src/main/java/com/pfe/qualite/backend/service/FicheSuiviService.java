package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.model.FicheSuivi;
import com.pfe.qualite.backend.repository.FicheQualiteRepository;
import com.pfe.qualite.backend.repository.FicheSuiviRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des fiches de suivi
 * Centralise la logique métier et les validations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheSuiviService {

    private final FicheSuiviRepository ficheSuiviRepository;
    private final FicheQualiteRepository ficheQualiteRepository;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;

    /**
     * Récupère toutes les fiches de suivi
     */
    public List<FicheSuivi> getAllFichesSuivi() {
        log.debug("Récupération de toutes les fiches de suivi");
        return ficheSuiviRepository.findAll();
    }

    /**
     * Récupère une fiche de suivi par son ID
     */
    public Optional<FicheSuivi> getFicheSuiviById(String id) {
        log.debug("Récupération de la fiche de suivi avec l'ID: {}", id);
        return ficheSuiviRepository.findById(id);
    }

    /**
     * Récupère les fiches de suivi par ID de fiche qualité
     */
    public List<FicheSuivi> getFichesSuiviByFicheId(String ficheId) {
        log.debug("Récupération des fiches de suivi pour la fiche qualité: {}", ficheId);
        return ficheSuiviRepository.findByFicheId(ficheId);
    }

    /**
     * Récupère les fiches de suivi par utilisateur
     */
    public List<FicheSuivi> getFichesSuiviByUtilisateur(String utilisateurId) {
        log.debug("Récupération des fiches de suivi pour l'utilisateur: {}", utilisateurId);
        return ficheSuiviRepository.findByAjoutePar(utilisateurId);
    }

    /**
     * Crée une nouvelle fiche de suivi
     */
    public FicheSuivi createFicheSuivi(FicheSuivi ficheSuivi, HttpServletRequest request) {
        log.info("Création d'une nouvelle fiche de suivi pour la fiche: {}", ficheSuivi.getFicheId());
        
        // Validation métier
        validateFicheSuivi(ficheSuivi);
        
        // Vérifier que la fiche qualité existe
        Optional<FicheQualite> ficheQualite = ficheQualiteRepository.findById(ficheSuivi.getFicheId());
        if (ficheQualite.isEmpty()) {
            throw new RuntimeException("La fiche qualité avec l'ID " + ficheSuivi.getFicheId() + " n'existe pas");
        }
        
        // Initialiser la date de suivi si non fournie
        if (ficheSuivi.getDateSuivi() == null) {
            ficheSuivi.setDateSuivi(new Date());
        }
        
        // Sauvegarde
        FicheSuivi savedFicheSuivi = ficheSuiviRepository.save(ficheSuivi);
        
        // Notification
        if (ficheSuivi.getAjoutePar() != null && !ficheSuivi.getAjoutePar().isEmpty()) {
            notificationService.creerNotification(
                "Nouvelle fiche de suivi ajoutée pour: " + ficheQualite.get().getTitre(),
                ficheSuivi.getAjoutePar(),
                "FICHE_SUIVI",
                savedFicheSuivi.getId()
            );
        }
        
        // Historique
        historiqueService.enregistrerAction(
            "CREATION",
            "FICHE_SUIVI",
            savedFicheSuivi.getId(),
            ficheSuivi.getAjoutePar(),
            "Création d'une fiche de suivi pour la fiche qualité: " + ficheSuivi.getFicheId(),
            request
        );
        
        log.info("Fiche de suivi créée avec succès, ID: {}", savedFicheSuivi.getId());
        return savedFicheSuivi;
    }

    /**
     * Met à jour une fiche de suivi existante
     */
    public FicheSuivi updateFicheSuivi(String id, FicheSuivi ficheSuiviUpdated, HttpServletRequest request) {
        log.info("Mise à jour de la fiche de suivi ID: {}", id);
        
        FicheSuivi existingFicheSuivi = ficheSuiviRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fiche de suivi non trouvée avec l'ID: " + id));
        
        // Validation métier
        validateFicheSuivi(ficheSuiviUpdated);
        
        // Mise à jour des champs
        existingFicheSuivi.setEtatAvancement(ficheSuiviUpdated.getEtatAvancement());
        existingFicheSuivi.setProblemes(ficheSuiviUpdated.getProblemes());
        existingFicheSuivi.setDecisions(ficheSuiviUpdated.getDecisions());
        existingFicheSuivi.setIndicateursKpi(ficheSuiviUpdated.getIndicateursKpi());
        existingFicheSuivi.setTauxConformite(ficheSuiviUpdated.getTauxConformite());
        existingFicheSuivi.setDelaiTraitementJours(ficheSuiviUpdated.getDelaiTraitementJours());
        existingFicheSuivi.setAjoutePar(ficheSuiviUpdated.getAjoutePar());
        
        if (ficheSuiviUpdated.getDateSuivi() != null) {
            existingFicheSuivi.setDateSuivi(ficheSuiviUpdated.getDateSuivi());
        }
        
        FicheSuivi savedFicheSuivi = ficheSuiviRepository.save(existingFicheSuivi);
        
        // Historique
        historiqueService.enregistrerAction(
            "MODIFICATION",
            "FICHE_SUIVI",
            savedFicheSuivi.getId(),
            savedFicheSuivi.getAjoutePar(),
            "Modification d'une fiche de suivi pour la fiche qualité: " + savedFicheSuivi.getFicheId(),
            request
        );
        
        log.info("Fiche de suivi mise à jour avec succès, ID: {}", savedFicheSuivi.getId());
        return savedFicheSuivi;
    }

    /**
     * Supprime une fiche de suivi
     */
    public void deleteFicheSuivi(String id, HttpServletRequest request) {
        log.info("Suppression de la fiche de suivi ID: {}", id);
        
        FicheSuivi ficheSuivi = ficheSuiviRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fiche de suivi non trouvée avec l'ID: " + id));
        
        // Historique avant suppression
        historiqueService.enregistrerAction(
            "SUPPRESSION",
            "FICHE_SUIVI",
            ficheSuivi.getId(),
            ficheSuivi.getAjoutePar(),
            "Suppression d'une fiche de suivi pour la fiche qualité: " + ficheSuivi.getFicheId(),
            request
        );
        
        ficheSuiviRepository.deleteById(id);
        log.info("Fiche de suivi supprimée avec succès, ID: {}", id);
    }

    /**
     * Compte le nombre total de fiches de suivi
     */
    public long countAllFichesSuivi() {
        return ficheSuiviRepository.count();
    }

    /**
     * Compte les fiches de suivi pour une fiche qualité
     */
    public long countFichesSuiviByFicheId(String ficheId) {
        return ficheSuiviRepository.findByFicheId(ficheId).size();
    }

    /**
     * Calcule le taux de conformité moyen
     */
    public Double calculateAverageTauxConformite() {
        List<FicheSuivi> fichesSuivi = ficheSuiviRepository.findAll();
        if (fichesSuivi.isEmpty()) {
            return 0.0;
        }
        
        double sum = fichesSuivi.stream()
            .filter(f -> f.getTauxConformite() != null)
            .mapToDouble(FicheSuivi::getTauxConformite)
            .sum();
        
        long count = fichesSuivi.stream()
            .filter(f -> f.getTauxConformite() != null)
            .count();
        
        return count > 0 ? sum / count : 0.0;
    }

    /**
     * Validation métier d'une fiche de suivi
     */
    private void validateFicheSuivi(FicheSuivi ficheSuivi) {
        if (ficheSuivi.getFicheId() == null || ficheSuivi.getFicheId().trim().isEmpty()) {
            throw new IllegalArgumentException("L'ID de la fiche qualité est obligatoire");
        }
        
        if (ficheSuivi.getTauxConformite() != null) {
            if (ficheSuivi.getTauxConformite() < 0 || ficheSuivi.getTauxConformite() > 100) {
                throw new IllegalArgumentException("Le taux de conformité doit être entre 0 et 100");
            }
        }
        
        if (ficheSuivi.getDelaiTraitementJours() != null && ficheSuivi.getDelaiTraitementJours() < 0) {
            throw new IllegalArgumentException("Le délai de traitement ne peut pas être négatif");
        }
        
        if (ficheSuivi.getEtatAvancement() != null && !isValidEtatAvancement(ficheSuivi.getEtatAvancement())) {
            throw new IllegalArgumentException("État d'avancement invalide: " + ficheSuivi.getEtatAvancement());
        }
    }

    /**
     * Vérifie si l'état d'avancement est valide
     */
    private boolean isValidEtatAvancement(String etatAvancement) {
        // Accepter les libellés français ET les codes
        return List.of(
            "EN_COURS", "TERMINE", "BLOQUE", "EN_ATTENTE", "VALIDE",
            "En cours", "Terminé", "Bloqué", "En attente", "Validé"
        ).contains(etatAvancement);
    }
}
