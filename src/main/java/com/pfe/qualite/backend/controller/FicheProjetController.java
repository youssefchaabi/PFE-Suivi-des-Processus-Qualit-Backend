package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.FicheProjet;
import com.pfe.qualite.backend.service.FicheProjetService;
import com.pfe.qualite.backend.service.HistoriqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.time.Instant;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/projets")
@CrossOrigin("*")
public class FicheProjetController {
    @Autowired
    private FicheProjetService ficheProjetService;

    @Autowired
    private HistoriqueService historiqueService;

    @GetMapping
    public List<FicheProjet> getAll() {
        return ficheProjetService.getAll();
    }

    @GetMapping("/{id}")
    public FicheProjet getById(@PathVariable String id) {
        return ficheProjetService.getById(id).orElseThrow(() -> new RuntimeException("Projet non trouvé"));
    }

    @PostMapping
    public FicheProjet create(@RequestBody FicheProjet ficheProjet, HttpServletRequest request) {
        // Correction robuste : convertir seulement si c'est une chaîne
        Object echeance = ficheProjet.getEcheance();
        if (echeance != null && echeance.getClass() == String.class) {
            try {
                ficheProjet.setEcheance(java.util.Date.from(Instant.parse((String) echeance)));
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Format de date d'échéance invalide. Utilisez le format ISO (yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ssZ)");
            }
        }
        
        FicheProjet saved = ficheProjetService.create(ficheProjet);
        
        // 📝 Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "CREATION",
                "FICHE_PROJET",
                saved.getId(),
                saved.getResponsable(),
                "Création du projet : " + saved.getNom(),
                request
        );
        
        return saved;
    }

    @PutMapping("/{id}")
    public FicheProjet update(@PathVariable String id, @RequestBody FicheProjet updated, HttpServletRequest request) {
        // Correction robuste : convertir seulement si c'est une chaîne
        Object echeance = updated.getEcheance();
        if (echeance != null && echeance.getClass() == String.class) {
            try {
                updated.setEcheance(java.util.Date.from(Instant.parse((String) echeance)));
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Format de date d'échéance invalide. Utilisez le format ISO (yyyy-MM-dd ou yyyy-MM-ddTHH:mm:ssZ)");
            }
        }
        
        FicheProjet saved = ficheProjetService.update(id, updated);
        
        // 📝 Enregistrer dans l'historique
        historiqueService.enregistrerAction(
                "MODIFICATION",
                "FICHE_PROJET",
                saved.getId(),
                saved.getResponsable(),
                "Modification du projet : " + saved.getNom(),
                request
        );
        
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, HttpServletRequest request) {
        ficheProjetService.getById(id).ifPresent(projet -> {
            // 📝 Enregistrer dans l'historique avant suppression
            historiqueService.enregistrerAction(
                    "SUPPRESSION",
                    "FICHE_PROJET",
                    projet.getId(),
                    projet.getResponsable(),
                    "Suppression du projet : " + projet.getNom(),
                    request
            );
        });
        
        ficheProjetService.delete(id);
    }
} 