package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.repository.FicheQualiteRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des fiches qualité
 * Centralise la logique métier et les validations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FicheQualiteService {

    private final FicheQualiteRepository ficheQualiteRepository;
    private final NotificationService notificationService;
    private final HistoriqueService historiqueService;

    /**
     * Récupère toutes les fiches qualité
     */
    public List<FicheQualite> getAllFiches() {
        log.debug("Récupération de toutes les fiches qualité");
        return ficheQualiteRepository.findAll();
    }

    /**
     * Récupère une fiche qualité par son ID
     */
    public Optional<FicheQualite> getFicheById(String id) {
        log.debug("Récupération de la fiche qualité avec l'ID: {}", id);
        return ficheQualiteRepository.findById(id);
    }

    /**
     * Récupère les fiches qualité par responsable
     */
    public List<FicheQualite> getFichesByResponsable(String responsableId) {
        log.debug("Récupération des fiches qualité pour le responsable: {}", responsableId);
        return ficheQualiteRepository.findByResponsable(responsableId);
    }

    /**
     * Récupère les fiches qualité par statut
     */
    public List<FicheQualite> getFichesByStatut(String statut) {
        log.debug("Récupération des fiches qualité avec le statut: {}", statut);
        return ficheQualiteRepository.findByStatut(statut);
    }

    /**
     * Crée une nouvelle fiche qualité
     */
    public FicheQualite createFiche(FicheQualite fiche, HttpServletRequest request) {
        log.info("Création d'une nouvelle fiche qualité: {}", fiche.getTitre());
        
        // Validation métier
        validateFiche(fiche);
        
        // Définir les métadonnées
        fiche.setDateCreation(java.time.LocalDateTime.now());
        fiche.setCreePar(fiche.getResponsable());
        
        // Sauvegarde
        FicheQualite savedFiche = ficheQualiteRepository.save(fiche);
        
        // Notification
        if (fiche.getResponsable() != null && !fiche.getResponsable().isEmpty()) {
            try {
                notificationService.creerNotification(
                    "Nouvelle fiche qualité créée: " + savedFiche.getTitre(),
                    fiche.getResponsable(),
                    "FICHE_QUALITE",
                    savedFiche.getId()
                );
            } catch (Exception e) {
                log.warn("Impossible de créer la notification: {}", e.getMessage());
            }
        }
        
        // Historique
        try {
            historiqueService.enregistrerAction(
                "CREATION",
                "FICHE_QUALITE",
                savedFiche.getId(),
                fiche.getResponsable(),
                "Création de la fiche qualité: " + savedFiche.getTitre(),
                request
            );
        } catch (Exception e) {
            log.warn("Impossible d'enregistrer l'historique: {}", e.getMessage());
        }
        
        log.info("Fiche qualité créée avec succès, ID: {}", savedFiche.getId());
        return savedFiche;
    }

    /**
     * Met à jour une fiche qualité existante
     */
    public FicheQualite updateFiche(String id, FicheQualite ficheUpdated, HttpServletRequest request) {
        log.info("Mise à jour de la fiche qualité ID: {}", id);
        
        FicheQualite existingFiche = ficheQualiteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fiche qualité non trouvée avec l'ID: " + id));
        
        // Validation métier
        validateFiche(ficheUpdated);
        
        // Mise à jour des champs
        existingFiche.setTitre(ficheUpdated.getTitre());
        existingFiche.setDescription(ficheUpdated.getDescription());
        existingFiche.setTypeFiche(ficheUpdated.getTypeFiche());
        existingFiche.setStatut(ficheUpdated.getStatut());
        existingFiche.setCategorie(ficheUpdated.getCategorie());
        existingFiche.setPriorite(ficheUpdated.getPriorite());
        existingFiche.setResponsable(ficheUpdated.getResponsable());
        existingFiche.setDateEcheance(ficheUpdated.getDateEcheance());
        existingFiche.setObservations(ficheUpdated.getObservations());
        
        // Métadonnées
        existingFiche.setDateModification(java.time.LocalDateTime.now());
        existingFiche.setModifiePar(ficheUpdated.getResponsable());
        
        FicheQualite savedFiche = ficheQualiteRepository.save(existingFiche);
        
        // Notification
        if (savedFiche.getResponsable() != null && !savedFiche.getResponsable().isEmpty()) {
            try {
                notificationService.creerNotification(
                    "Fiche qualité mise à jour: " + savedFiche.getTitre(),
                    savedFiche.getResponsable(),
                    "FICHE_QUALITE",
                    savedFiche.getId()
                );
            } catch (Exception e) {
                log.warn("Impossible de créer la notification: {}", e.getMessage());
            }
        }
        
        // Historique
        try {
            historiqueService.enregistrerAction(
                "MODIFICATION",
                "FICHE_QUALITE",
                savedFiche.getId(),
                savedFiche.getResponsable(),
                "Modification de la fiche qualité: " + savedFiche.getTitre(),
                request
            );
        } catch (Exception e) {
            log.warn("Impossible d'enregistrer l'historique: {}", e.getMessage());
        }
        
        log.info("Fiche qualité mise à jour avec succès, ID: {}", savedFiche.getId());
        return savedFiche;
    }

    /**
     * Supprime une fiche qualité
     */
    public void deleteFiche(String id, HttpServletRequest request) {
        log.info("Suppression de la fiche qualité ID: {}", id);
        
        FicheQualite fiche = ficheQualiteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fiche qualité non trouvée avec l'ID: " + id));
        
        // Historique avant suppression
        historiqueService.enregistrerAction(
            "SUPPRESSION",
            "FICHE_QUALITE",
            fiche.getId(),
            fiche.getResponsable(),
            "Suppression de la fiche qualité: " + fiche.getTitre(),
            request
        );
        
        ficheQualiteRepository.deleteById(id);
        log.info("Fiche qualité supprimée avec succès, ID: {}", id);
    }

    /**
     * Compte le nombre total de fiches qualité
     */
    public long countAllFiches() {
        return ficheQualiteRepository.count();
    }

    /**
     * Compte les fiches par statut
     */
    public long countFichesByStatut(String statut) {
        return ficheQualiteRepository.findByStatut(statut).size();
    }

    /**
     * Validation métier d'une fiche qualité
     */
    private void validateFiche(FicheQualite fiche) {
        if (fiche.getTitre() == null || fiche.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la fiche est obligatoire");
        }
        
        if (fiche.getTitre().length() < 3) {
            throw new IllegalArgumentException("Le titre doit contenir au moins 3 caractères");
        }
        
        if (fiche.getTitre().length() > 200) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser 200 caractères");
        }
        
        if (fiche.getDescription() == null || fiche.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        
        if (fiche.getDescription().length() < 10) {
            throw new IllegalArgumentException("La description doit contenir au moins 10 caractères");
        }
        
        if (fiche.getTypeFiche() == null || fiche.getTypeFiche().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de fiche est obligatoire");
        }
        
        if (fiche.getStatut() == null || fiche.getStatut().trim().isEmpty()) {
            throw new IllegalArgumentException("Le statut est obligatoire");
        }
        
        if (fiche.getResponsable() == null || fiche.getResponsable().trim().isEmpty()) {
            throw new IllegalArgumentException("Le responsable est obligatoire");
        }
        
        if (fiche.getDateEcheance() == null) {
            throw new IllegalArgumentException("La date d'échéance est obligatoire");
        }
    }

    /**
     * Vérifie si le statut est valide
     */
    private boolean isValidStatut(String statut) {
        return List.of("EN_COURS", "TERMINEE", "VALIDEE", "REJETEE", "EN_ATTENTE", "BLOQUEE")
            .contains(statut.toUpperCase());
    }

    /**
     * Vérifie si le type de fiche est valide
     */
    private boolean isValidTypeFiche(String typeFiche) {
        return List.of("CONTROLE", "AUDIT", "AMELIORATION", "FORMATION", "MAINTENANCE", "AUTRE")
            .contains(typeFiche.toUpperCase());
    }
}
