package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.service.FicheQualiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des fiches qualité
 * Utilise FicheQualiteService pour la logique métier
 */
@RestController
@RequestMapping("/api/fiches")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FicheQualiteController {

    private final FicheQualiteService ficheQualiteService;

    /**
     * Récupère toutes les fiches qualité
     */
    @GetMapping
    public ResponseEntity<List<FicheQualite>> getAll() {
        List<FicheQualite> fiches = ficheQualiteService.getAllFiches();
        return ResponseEntity.ok(fiches);
    }

    /**
     * Récupère une fiche qualité par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FicheQualite> getById(@PathVariable String id) {
        return ficheQualiteService.getFicheById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère les fiches qualité par responsable
     */
    @GetMapping("/responsable/{responsableId}")
    public ResponseEntity<List<FicheQualite>> getByResponsable(@PathVariable String responsableId) {
        List<FicheQualite> fiches = ficheQualiteService.getFichesByResponsable(responsableId);
        return ResponseEntity.ok(fiches);
    }

    /**
     * Récupère les fiches qualité par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<FicheQualite>> getByStatut(@PathVariable String statut) {
        List<FicheQualite> fiches = ficheQualiteService.getFichesByStatut(statut);
        return ResponseEntity.ok(fiches);
    }

    /**
     * Crée une nouvelle fiche qualité
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody FicheQualite fiche, HttpServletRequest request) {
        try {
            FicheQualite savedFiche = ficheQualiteService.createFiche(fiche, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFiche);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Met à jour une fiche qualité existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id, 
            @RequestBody FicheQualite updated, 
            HttpServletRequest request) {
        try {
            FicheQualite updatedFiche = ficheQualiteService.updateFiche(id, updated, request);
            return ResponseEntity.ok(updatedFiche);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une fiche qualité
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        try {
            ficheQualiteService.deleteFiche(id, request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Compte le nombre total de fiches qualité
     */
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = ficheQualiteService.countAllFiches();
        return ResponseEntity.ok(count);
    }

    /**
     * Compte les fiches par statut
     */
    @GetMapping("/count/statut/{statut}")
    public ResponseEntity<Long> countByStatut(@PathVariable String statut) {
        long count = ficheQualiteService.countFichesByStatut(statut);
        return ResponseEntity.ok(count);
    }
}
