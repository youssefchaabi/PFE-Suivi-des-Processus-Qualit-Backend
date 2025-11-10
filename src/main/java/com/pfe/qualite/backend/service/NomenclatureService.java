package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.Nomenclature;
import com.pfe.qualite.backend.repository.NomenclatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des nomenclatures
 * Centralise la logique métier et les validations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NomenclatureService {

    private final NomenclatureRepository nomenclatureRepository;

    /**
     * Récupère toutes les nomenclatures
     */
    public List<Nomenclature> getAllNomenclatures() {
        log.debug("Récupération de toutes les nomenclatures");
        return nomenclatureRepository.findAll();
    }

    /**
     * Récupère une nomenclature par son ID
     */
    public Optional<Nomenclature> getNomenclatureById(String id) {
        log.debug("Récupération de la nomenclature avec l'ID: {}", id);
        return nomenclatureRepository.findById(id);
    }

    /**
     * Récupère les nomenclatures par type
     */
    public List<Nomenclature> getNomenclaturesByType(String type) {
        log.debug("Récupération des nomenclatures de type: {}", type);
        return nomenclatureRepository.findByType(type);
    }

    /**
     * Crée une nouvelle nomenclature
     */
    public Nomenclature createNomenclature(Nomenclature nomenclature) {
        log.info("Création d'une nouvelle nomenclature: {} - {}", nomenclature.getType(), nomenclature.getCode());
        
        // Validation métier
        validateNomenclature(nomenclature);
        
        // Vérifier si la nomenclature existe déjà
        List<Nomenclature> existing = nomenclatureRepository.findByType(nomenclature.getType());
        boolean alreadyExists = existing.stream()
            .anyMatch(n -> n.getCode() != null && nomenclature.getCode() != null && 
                          n.getCode().equalsIgnoreCase(nomenclature.getCode()));
        
        if (alreadyExists) {
            throw new IllegalArgumentException(
                "Une nomenclature avec le type '" + nomenclature.getType() + 
                "' et le code '" + nomenclature.getCode() + "' existe déjà"
            );
        }
        
        // Définir les valeurs par défaut
        if (nomenclature.getActif() == null) {
            nomenclature.setActif(true);
        }
        nomenclature.setDateCreation(java.time.LocalDateTime.now());
        
        Nomenclature savedNomenclature = nomenclatureRepository.save(nomenclature);
        log.info("Nomenclature créée avec succès, ID: {}", savedNomenclature.getId());
        return savedNomenclature;
    }

    /**
     * Met à jour une nomenclature existante
     */
    public Nomenclature updateNomenclature(String id, Nomenclature nomenclatureUpdated) {
        log.info("=== SERVICE: Mise à jour nomenclature ID: {} ===", id);
        log.info("Données reçues: type={}, code={}, libelle={}, actif={}", 
            nomenclatureUpdated.getType(), 
            nomenclatureUpdated.getCode(), 
            nomenclatureUpdated.getLibelle(), 
            nomenclatureUpdated.getActif());
        
        // Vérifier si l'ID existe
        log.info("Recherche nomenclature avec ID: {}", id);
        Optional<Nomenclature> optionalNomenclature = nomenclatureRepository.findById(id);
        
        if (!optionalNomenclature.isPresent()) {
            log.error("ERREUR: Nomenclature ID={} NON TROUVÉE dans la base de données", id);
            log.error("IDs disponibles dans la base:");
            nomenclatureRepository.findAll().forEach(n -> 
                log.error("  - ID: {}, Type: {}, Code: {}", n.getId(), n.getType(), n.getCode())
            );
            throw new RuntimeException("Nomenclature non trouvée avec l'ID: " + id);
        }
        
        Nomenclature existingNomenclature = optionalNomenclature.get();
        log.info("Nomenclature trouvée: type={}, code={}", existingNomenclature.getType(), existingNomenclature.getCode());
        
        // Validation métier
        validateNomenclature(nomenclatureUpdated);
        
        // Vérifier les doublons (sauf pour l'enregistrement actuel)
        List<Nomenclature> existing = nomenclatureRepository.findByType(nomenclatureUpdated.getType());
        boolean duplicateExists = existing.stream()
            .filter(n -> !n.getId().equals(id))
            .anyMatch(n -> n.getCode() != null && nomenclatureUpdated.getCode() != null && 
                          n.getCode().equalsIgnoreCase(nomenclatureUpdated.getCode()));
        
        if (duplicateExists) {
            log.error("ERREUR: Doublon détecté pour type={}, code={}", nomenclatureUpdated.getType(), nomenclatureUpdated.getCode());
            throw new IllegalArgumentException(
                "Une autre nomenclature avec le type '" + nomenclatureUpdated.getType() + 
                "' et le code '" + nomenclatureUpdated.getCode() + "' existe déjà"
            );
        }
        
        // Mise à jour des champs
        log.info("Mise à jour des champs...");
        existingNomenclature.setType(nomenclatureUpdated.getType());
        existingNomenclature.setCode(nomenclatureUpdated.getCode());
        existingNomenclature.setLibelle(nomenclatureUpdated.getLibelle());
        existingNomenclature.setDescription(nomenclatureUpdated.getDescription());
        existingNomenclature.setActif(nomenclatureUpdated.getActif());
        existingNomenclature.setOrdre(nomenclatureUpdated.getOrdre());
        existingNomenclature.setDateModification(java.time.LocalDateTime.now());
        
        Nomenclature savedNomenclature = nomenclatureRepository.save(existingNomenclature);
        log.info("=== Nomenclature mise à jour avec succès, ID: {} ===", savedNomenclature.getId());
        return savedNomenclature;
    }

    /**
     * Supprime une nomenclature
     */
    public void deleteNomenclature(String id) {
        log.info("Suppression de la nomenclature ID: {}", id);
        
        if (!nomenclatureRepository.existsById(id)) {
            throw new RuntimeException("Nomenclature non trouvée avec l'ID: " + id);
        }
        
        nomenclatureRepository.deleteById(id);
        log.info("Nomenclature supprimée avec succès, ID: {}", id);
    }

    /**
     * Compte le nombre total de nomenclatures
     */
    public long countAllNomenclatures() {
        return nomenclatureRepository.count();
    }

    /**
     * Compte les nomenclatures par type
     */
    public long countNomenclaturesByType(String type) {
        return nomenclatureRepository.findByType(type).size();
    }

    /**
     * Récupère tous les types de nomenclatures distincts
     */
    public List<String> getAllTypes() {
        return nomenclatureRepository.findAll().stream()
            .map(Nomenclature::getType)
            .distinct()
            .sorted()
            .toList();
    }

    /**
     * Initialise les nomenclatures par défaut si elles n'existent pas
     */
    public void initializeDefaultNomenclatures() {
        log.info("Initialisation des nomenclatures par défaut");
        
        // NETTOYER LES NOMENCLATURES INVALIDES (code null)
        try {
            List<Nomenclature> allNomenclatures = nomenclatureRepository.findAll();
            long invalidCount = allNomenclatures.stream()
                .filter(n -> n.getCode() == null || n.getCode().trim().isEmpty())
                .count();
            
            if (invalidCount > 0) {
                log.warn("⚠️ {} nomenclatures invalides détectées (code null ou vide)", invalidCount);
                allNomenclatures.stream()
                    .filter(n -> n.getCode() == null || n.getCode().trim().isEmpty())
                    .forEach(n -> {
                        log.warn("Suppression nomenclature invalide: ID={}, Type={}, Libelle={}", 
                            n.getId(), n.getType(), n.getLibelle());
                        nomenclatureRepository.deleteById(n.getId());
                    });
                log.info("✅ Nomenclatures invalides nettoyées");
            }
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des nomenclatures invalides", e);
        }
        
        // Statuts des fiches
        createIfNotExists("STATUT", "EN_COURS", "En cours");
        createIfNotExists("STATUT", "TERMINEE", "Terminée");
        createIfNotExists("STATUT", "VALIDEE", "Validée");
        createIfNotExists("STATUT", "REJETEE", "Rejetée");
        createIfNotExists("STATUT", "EN_ATTENTE", "En attente");
        createIfNotExists("STATUT", "BLOQUEE", "Bloquée");
        
        // Types de fiches
        createIfNotExists("TYPE_FICHE", "FICHE_PROJET", "Fiche Projet");
        createIfNotExists("TYPE_FICHE", "FICHE_SUIVI", "Fiche de Suivi");
        createIfNotExists("TYPE_FICHE", "CONTROLE", "Contrôle");
        createIfNotExists("TYPE_FICHE", "AUDIT", "Audit");
        createIfNotExists("TYPE_FICHE", "AMELIORATION", "Amélioration");
        createIfNotExists("TYPE_FICHE", "FORMATION", "Formation");
        createIfNotExists("TYPE_FICHE", "MAINTENANCE", "Maintenance");
        createIfNotExists("TYPE_FICHE", "AUTRE", "Autre");
        
        // Catégories de projets
        createIfNotExists("CATEGORIE_PROJET", "DEVELOPPEMENT", "Développement");
        createIfNotExists("CATEGORIE_PROJET", "INFRASTRUCTURE", "Infrastructure");
        createIfNotExists("CATEGORIE_PROJET", "QUALITE", "Qualité");
        createIfNotExists("CATEGORIE_PROJET", "SECURITE", "Sécurité");
        createIfNotExists("CATEGORIE_PROJET", "FORMATION", "Formation");
        
        // Priorités
        createIfNotExists("PRIORITE", "HAUTE", "Haute");
        createIfNotExists("PRIORITE", "MOYENNE", "Moyenne");
        createIfNotExists("PRIORITE", "BASSE", "Basse");
        
        log.info("Nomenclatures par défaut initialisées");
    }

    /**
     * Crée une nomenclature si elle n'existe pas déjà
     */
    private void createIfNotExists(String type, String code, String libelle) {
        List<Nomenclature> existing = nomenclatureRepository.findByType(type);
        boolean exists = existing.stream()
            .filter(n -> n.getCode() != null) // Filtrer les codes null
            .anyMatch(n -> n.getCode().equalsIgnoreCase(code));
        
        if (!exists) {
            Nomenclature nomenclature = Nomenclature.builder()
                .type(type)
                .code(code)
                .libelle(libelle)
                .actif(true)
                .dateCreation(java.time.LocalDateTime.now())
                .build();
            nomenclatureRepository.save(nomenclature);
            log.debug("Nomenclature créée: {} - {}", type, code);
        }
    }

    /**
     * Validation métier d'une nomenclature
     */
    private void validateNomenclature(Nomenclature nomenclature) {
        if (nomenclature.getType() == null || nomenclature.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de la nomenclature est obligatoire");
        }
        
        if (nomenclature.getCode() == null || nomenclature.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Le code de la nomenclature est obligatoire");
        }
        
        if (nomenclature.getLibelle() == null || nomenclature.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé de la nomenclature est obligatoire");
        }
        
        if (nomenclature.getType().length() > 50) {
            throw new IllegalArgumentException("Le type ne peut pas dépasser 50 caractères");
        }
        
        if (nomenclature.getCode().length() > 50) {
            throw new IllegalArgumentException("Le code ne peut pas dépasser 50 caractères");
        }
        
        if (nomenclature.getLibelle().length() > 100) {
            throw new IllegalArgumentException("Le libellé ne peut pas dépasser 100 caractères");
        }
        
        // Normaliser
        nomenclature.setType(nomenclature.getType().toUpperCase().trim());
        nomenclature.setCode(nomenclature.getCode().toUpperCase().trim());
        nomenclature.setLibelle(nomenclature.getLibelle().trim());
    }
}
