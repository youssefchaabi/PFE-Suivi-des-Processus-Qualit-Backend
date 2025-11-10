package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.Utilisateur;
import com.pfe.qualite.backend.service.UtilisateurService;
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
 * Contrôleur pour la gestion des utilisateurs (ADMIN uniquement)
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final HistoriqueService historiqueService;
    private final JwtUtil jwtUtil;

    /**
     * Récupérer tous les utilisateurs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        log.info("GET /api/utilisateurs - Récupération de tous les utilisateurs");
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
        
        // Ne pas retourner les mots de passe
        utilisateurs.forEach(u -> u.setPassword(null));
        
        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Récupérer un utilisateur par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable String id) {
        log.info("GET /api/utilisateurs/{} - Récupération utilisateur", id);
        
        return utilisateurService.getUtilisateurById(id)
                .map(user -> {
                    user.setPassword(null); // Ne pas retourner le mot de passe
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Créer un nouvel utilisateur
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Utilisateur> createUtilisateur(
            @RequestBody Utilisateur utilisateur,
            HttpServletRequest request) {
        
        log.info("POST /api/utilisateurs - Création utilisateur: {}", utilisateur.getEmail());
        
        try {
            // Récupérer l'ID de l'admin depuis le token JWT
            String adminId = jwtUtil.extractUserIdFromRequest(request);
            
            Utilisateur created = utilisateurService.creerUtilisateur(utilisateur, adminId);
            
            // Historique
            historiqueService.enregistrerAction(
                    "CREATION",
                    "UTILISATEUR",
                    created.getId(),
                    adminId,
                    "Création de l'utilisateur: " + created.getNom() + " (" + created.getRole() + ")",
                    request
            );
            
            created.setPassword(null); // Ne pas retourner le mot de passe
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (RuntimeException e) {
            log.error("Erreur lors de la création de l'utilisateur", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mettre à jour un utilisateur
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Utilisateur> updateUtilisateur(
            @PathVariable String id,
            @RequestBody Utilisateur utilisateur,
            HttpServletRequest request) {
        
        log.info("PUT /api/utilisateurs/{} - Mise à jour utilisateur", id);
        
        try {
            Utilisateur updated = utilisateurService.updateUtilisateur(id, utilisateur);
            
            // Historique
            historiqueService.enregistrerAction(
                    "MODIFICATION",
                    "UTILISATEUR",
                    updated.getId(),
                    jwtUtil.extractUserIdFromRequest(request),
                    "Modification de l'utilisateur: " + updated.getNom(),
                    request
            );
            
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
            
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Utilisateur> toggleActif(
            @PathVariable String id,
            HttpServletRequest request) {
        
        log.info("PATCH /api/utilisateurs/{}/toggle - Toggle actif", id);
        
        try {
            Utilisateur updated = utilisateurService.toggleActif(id);
            
            // Historique
            String action = updated.getActif() ? "Activation" : "Désactivation";
            historiqueService.enregistrerAction(
                    "MODIFICATION",
                    "UTILISATEUR",
                    updated.getId(),
                    jwtUtil.extractUserIdFromRequest(request),
                    action + " de l'utilisateur: " + updated.getNom(),
                    request
            );
            
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
            
        } catch (RuntimeException e) {
            log.error("Erreur lors du toggle actif", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable String id,
            HttpServletRequest request) {
        
        log.info("POST /api/utilisateurs/{}/reset-password - Reset mot de passe", id);
        
        try {
            String nouveauMotDePasse = utilisateurService.resetMotDePasse(id);
            
            // Historique
            historiqueService.enregistrerAction(
                    "MODIFICATION",
                    "UTILISATEUR",
                    id,
                    jwtUtil.extractUserIdFromRequest(request),
                    "Réinitialisation du mot de passe",
                    request
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe réinitialisé avec succès");
            response.put("nouveauMotDePasse", nouveauMotDePasse);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Erreur lors de la réinitialisation du mot de passe", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer un utilisateur
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUtilisateur(
            @PathVariable String id,
            HttpServletRequest request) {
        
        log.info("DELETE /api/utilisateurs/{} - Suppression utilisateur", id);
        
        try {
            // Récupérer l'utilisateur avant suppression pour l'historique
            String userId = jwtUtil.extractUserIdFromRequest(request);
            utilisateurService.getUtilisateurById(id).ifPresent(user -> {
                historiqueService.enregistrerAction(
                        "SUPPRESSION",
                        "UTILISATEUR",
                        user.getId(),
                        userId,
                        "Suppression de l'utilisateur: " + user.getNom(),
                        request
                );
            });
            
            utilisateurService.deleteUtilisateur(id);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de l'utilisateur", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Statistiques des utilisateurs
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("GET /api/utilisateurs/stats - Statistiques utilisateurs");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", utilisateurService.getAllUtilisateurs().size());
        stats.put("actifs", utilisateurService.countActifs());
        stats.put("admins", utilisateurService.countByRole("ADMIN"));
        stats.put("chefsProjet", utilisateurService.countByRole("CHEF_PROJET"));
        stats.put("pilotesQualite", utilisateurService.countByRole("PILOTE_QUALITE"));
        
        return ResponseEntity.ok(stats);
    }
}
