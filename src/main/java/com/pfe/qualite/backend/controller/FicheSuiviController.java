package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.FicheSuivi;
import com.pfe.qualite.backend.service.FicheSuiviService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des fiches de suivi
 * Utilise FicheSuiviService pour la logique métier
 */
@RestController
@RequestMapping("/api/suivis")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FicheSuiviController {

    private final FicheSuiviService ficheSuiviService;

    /**
     * Récupère toutes les fiches de suivi
     */
    @GetMapping
    public ResponseEntity<List<FicheSuivi>> getAll() {
        List<FicheSuivi> fichesSuivi = ficheSuiviService.getAllFichesSuivi();
        return ResponseEntity.ok(fichesSuivi);
    }

    /**
     * Récupère une fiche de suivi par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FicheSuivi> getById(@PathVariable String id) {
        return ficheSuiviService.getFicheSuiviById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère les fiches de suivi par ID de fiche qualité
     */
    @GetMapping("/fiche/{ficheId}")
    public ResponseEntity<List<FicheSuivi>> getByFicheId(@PathVariable String ficheId) {
        List<FicheSuivi> fichesSuivi = ficheSuiviService.getFichesSuiviByFicheId(ficheId);
        return ResponseEntity.ok(fichesSuivi);
    }

    /**
     * Récupère les fiches de suivi par utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<FicheSuivi>> getByUtilisateur(@PathVariable String utilisateurId) {
        List<FicheSuivi> fichesSuivi = ficheSuiviService.getFichesSuiviByUtilisateur(utilisateurId);
        return ResponseEntity.ok(fichesSuivi);
    }

    /**
     * Crée une nouvelle fiche de suivi
     */
    @PostMapping
    public ResponseEntity<FicheSuivi> create(@RequestBody FicheSuivi ficheSuivi, HttpServletRequest request) {
        try {
            FicheSuivi savedFicheSuivi = ficheSuiviService.createFicheSuivi(ficheSuivi, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFicheSuivi);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Met à jour une fiche de suivi existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<FicheSuivi> update(
            @PathVariable String id, 
            @RequestBody FicheSuivi updated, 
            HttpServletRequest request) {
        try {
            FicheSuivi updatedFicheSuivi = ficheSuiviService.updateFicheSuivi(id, updated, request);
            return ResponseEntity.ok(updatedFicheSuivi);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une fiche de suivi
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        try {
            ficheSuiviService.deleteFicheSuivi(id, request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Compte le nombre total de fiches de suivi
     */
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = ficheSuiviService.countAllFichesSuivi();
        return ResponseEntity.ok(count);
    }

    /**
     * Compte les fiches de suivi pour une fiche qualité
     */
    @GetMapping("/count/fiche/{ficheId}")
    public ResponseEntity<Long> countByFicheId(@PathVariable String ficheId) {
        long count = ficheSuiviService.countFichesSuiviByFicheId(ficheId);
        return ResponseEntity.ok(count);
    }

    /**
     * Calcule le taux de conformité moyen
     */
    @GetMapping("/stats/taux-conformite-moyen")
    public ResponseEntity<Double> getAverageTauxConformite() {
        Double average = ficheSuiviService.calculateAverageTauxConformite();
        return ResponseEntity.ok(average);
    }
}
