package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.Nomenclature;
import com.pfe.qualite.backend.service.NomenclatureService;
import com.pfe.qualite.backend.service.HistoriqueService;
import com.pfe.qualite.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des nomenclatures (ADMIN uniquement)
 * Utilise NomenclatureService pour la logique métier
 */
@RestController
@RequestMapping("/api/nomenclatures")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class NomenclatureController {

    private final NomenclatureService nomenclatureService;
    private final HistoriqueService historiqueService;
    private final JwtUtil jwtUtil;

    /**
     * Récupère toutes les nomenclatures
     */
    @GetMapping
    public ResponseEntity<List<Nomenclature>> getAll() {
        List<Nomenclature> nomenclatures = nomenclatureService.getAllNomenclatures();
        return ResponseEntity.ok(nomenclatures);
    }

    /**
     * Récupère une nomenclature par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Nomenclature> getById(@PathVariable String id) {
        return nomenclatureService.getNomenclatureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère les nomenclatures par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Nomenclature>> getByType(@PathVariable String type) {
        List<Nomenclature> nomenclatures = nomenclatureService.getNomenclaturesByType(type);
        return ResponseEntity.ok(nomenclatures);
    }

    /**
     * Récupère tous les types de nomenclatures distincts
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllTypes() {
        List<String> types = nomenclatureService.getAllTypes();
        return ResponseEntity.ok(types);
    }

    /**
     * Crée une nouvelle nomenclature
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(
            @RequestBody Nomenclature nomenclature,
            HttpServletRequest request) {
        try {
            log.info("=== POST /api/nomenclatures ===");
            log.info("Données reçues: type={}, code={}, libelle={}, actif={}", 
                nomenclature.getType(), 
                nomenclature.getCode(), 
                nomenclature.getLibelle(), 
                nomenclature.getActif());
            
            Nomenclature savedNomenclature = nomenclatureService.createNomenclature(nomenclature);
            
            log.info("Nomenclature créée avec succès: ID={}", savedNomenclature.getId());
            
            // Historique (ne doit pas bloquer la création)
            try {
                historiqueService.enregistrerAction(
                        "CREATION",
                        "NOMENCLATURE",
                        savedNomenclature.getId(),
                        jwtUtil.extractUserIdFromRequest(request),
                        "Création nomenclature: " + savedNomenclature.getType() + " - " + savedNomenclature.getLibelle(),
                        request
                );
            } catch (Exception histEx) {
                log.warn("Impossible d'enregistrer l'historique: {}", histEx.getMessage());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedNomenclature);
        } catch (IllegalArgumentException e) {
            log.error("ERREUR VALIDATION: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("ERREUR INATTENDUE lors de la création: {}", e.getMessage(), e);
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erreur serveur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Met à jour une nomenclature existante
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestBody Nomenclature updated,
            HttpServletRequest request) {
        try {
            log.info("=== PUT /api/nomenclatures/{} ===", id);
            log.info("Données reçues: type={}, code={}, libelle={}, actif={}", 
                updated.getType(), updated.getCode(), updated.getLibelle(), updated.getActif());
            
            Nomenclature updatedNomenclature = nomenclatureService.updateNomenclature(id, updated);
            
            log.info("Nomenclature mise à jour avec succès: ID={}", updatedNomenclature.getId());
            
            // Historique (ne doit pas bloquer la modification)
            try {
                historiqueService.enregistrerAction(
                        "MODIFICATION",
                        "NOMENCLATURE",
                        updatedNomenclature.getId(),
                        jwtUtil.extractUserIdFromRequest(request),
                        "Modification nomenclature: " + updatedNomenclature.getType() + " - " + updatedNomenclature.getLibelle(),
                        request
                );
            } catch (Exception histEx) {
                log.warn("Impossible d'enregistrer l'historique: {}", histEx.getMessage());
            }
            
            return ResponseEntity.ok(updatedNomenclature);
        } catch (IllegalArgumentException e) {
            log.error("ERREUR VALIDATION: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("ERREUR: Nomenclature ID={} NON TROUVÉE: {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Nomenclature non trouvée avec l'ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("ERREUR INATTENDUE lors de la modification: {}", e.getMessage(), e);
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erreur serveur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Supprime une nomenclature
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            HttpServletRequest request) {
        try {
            log.info("DELETE /api/nomenclatures/{} - Suppression nomenclature", id);
            
            // Récupérer la nomenclature avant suppression pour l'historique
            String userId = jwtUtil.extractUserIdFromRequest(request);
            nomenclatureService.getNomenclatureById(id).ifPresent(nom -> {
                historiqueService.enregistrerAction(
                        "SUPPRESSION",
                        "NOMENCLATURE",
                        nom.getId(),
                        userId,
                        "Suppression nomenclature: " + nom.getType() + " - " + nom.getLibelle(),
                        request
                );
            });
            
            nomenclatureService.deleteNomenclature(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur suppression nomenclature", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Compte le nombre total de nomenclatures
     */
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = nomenclatureService.countAllNomenclatures();
        return ResponseEntity.ok(count);
    }

    /**
     * Compte les nomenclatures par type
     */
    @GetMapping("/count/type/{type}")
    public ResponseEntity<Long> countByType(@PathVariable String type) {
        long count = nomenclatureService.countNomenclaturesByType(type);
        return ResponseEntity.ok(count);
    }

    /**
     * Initialise les nomenclatures par défaut
     */
    @PostMapping("/init-defaults")
    public ResponseEntity<Void> initializeDefaults() {
        nomenclatureService.initializeDefaultNomenclatures();
        return ResponseEntity.ok().build();
    }
}
