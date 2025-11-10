package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.Tache;
import com.pfe.qualite.backend.model.TacheStats;
import com.pfe.qualite.backend.service.TacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/taches")
@CrossOrigin(origins = "*")
public class TacheController {
    
    @Autowired
    private TacheService tacheService;
    
    /**
     * Récupérer toutes les tâches
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PILOTE_QUALITE')")
    public ResponseEntity<List<Tache>> getAllTaches() {
        List<Tache> taches = tacheService.getAllTaches();
        return ResponseEntity.ok(taches);
    }
    
    /**
     * Récupérer les tâches d'un utilisateur
     */
    @GetMapping("/utilisateur/{userId}")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN', 'PILOTE_QUALITE')")
    public ResponseEntity<List<Tache>> getTachesByUtilisateur(@PathVariable String userId) {
        List<Tache> taches = tacheService.getTachesByUtilisateur(userId);
        return ResponseEntity.ok(taches);
    }
    
    /**
     * Récupérer une tâche par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN', 'PILOTE_QUALITE')")
    public ResponseEntity<Tache> getTacheById(@PathVariable String id) {
        Optional<Tache> tache = tacheService.getTacheById(id);
        return tache.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Créer une nouvelle tâche
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN')")
    public ResponseEntity<Tache> createTache(@RequestBody Tache tache) {
        try {
            Tache created = tacheService.createTache(tache);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mettre à jour une tâche
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN')")
    public ResponseEntity<Tache> updateTache(@PathVariable String id, @RequestBody Tache tache) {
        try {
            Tache updated = tacheService.updateTache(id, tache);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Supprimer une tâche
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN')")
    public ResponseEntity<Void> deleteTache(@PathVariable String id) {
        try {
            tacheService.deleteTache(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Récupérer les tâches d'un projet
     */
    @GetMapping("/projet/{projetId}")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN', 'PILOTE_QUALITE')")
    public ResponseEntity<List<Tache>> getTachesByProjet(@PathVariable String projetId) {
        List<Tache> taches = tacheService.getTachesByProjet(projetId);
        return ResponseEntity.ok(taches);
    }
    
    /**
     * Récupérer les statistiques des tâches d'un utilisateur
     */
    @GetMapping("/stats/{userId}")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN', 'PILOTE_QUALITE')")
    public ResponseEntity<TacheStats> getStatistiques(@PathVariable String userId) {
        TacheStats stats = tacheService.getStatistiques(userId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Marquer une tâche comme terminée
     */
    @PutMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('CHEF_PROJET', 'ADMIN')")
    public ResponseEntity<Tache> marquerTerminee(@PathVariable String id, @RequestParam String userId) {
        try {
            Tache updated = tacheService.marquerTerminee(id, userId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
