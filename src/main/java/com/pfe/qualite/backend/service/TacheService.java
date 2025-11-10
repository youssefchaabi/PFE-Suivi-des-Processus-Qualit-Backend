package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.*;
import com.pfe.qualite.backend.repository.TacheRepository;
import com.pfe.qualite.backend.repository.FicheProjetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TacheService {
    
    @Autowired
    private TacheRepository tacheRepository;
    
    @Autowired
    private FicheProjetRepository ficheProjetRepository;
    
    /**
     * Récupérer toutes les tâches
     */
    public List<Tache> getAllTaches() {
        List<Tache> taches = tacheRepository.findAll();
        verifierEtMarquerRetards(taches);
        return taches;
    }
    
    /**
     * Récupérer les tâches d'un utilisateur
     */
    public List<Tache> getTachesByUtilisateur(String userId) {
        List<Tache> taches = tacheRepository.findByCreePar(userId);
        verifierEtMarquerRetards(taches);
        return taches;
    }
    
    /**
     * Récupérer une tâche par ID
     */
    public Optional<Tache> getTacheById(String id) {
        return tacheRepository.findById(id);
    }
    
    /**
     * Créer une nouvelle tâche
     */
    public Tache createTache(Tache tache) {
        // Récupérer le nom du projet
        if (tache.getProjetId() != null) {
            Optional<FicheProjet> projet = ficheProjetRepository.findById(tache.getProjetId());
            projet.ifPresent(p -> tache.setProjetNom(p.getNom()));
        }
        
        tache.setDateCreation(LocalDateTime.now());
        
        // Vérifier si déjà en retard
        if (tache.getDateEcheance() != null && 
            tache.getDateEcheance().isBefore(LocalDate.now()) &&
            tache.getStatut() != StatutTache.TERMINEE) {
            tache.setStatut(StatutTache.EN_RETARD);
        }
        
        Tache saved = tacheRepository.save(tache);
        
        // TODO: Enregistrer dans l'historique si nécessaire
        
        return saved;
    }
    
    /**
     * Mettre à jour une tâche
     */
    public Tache updateTache(String id, Tache tache) {
        Optional<Tache> existingOpt = tacheRepository.findById(id);
        
        if (existingOpt.isPresent()) {
            Tache existing = existingOpt.get();
            
            existing.setTitre(tache.getTitre());
            existing.setDescription(tache.getDescription());
            existing.setProjetId(tache.getProjetId());
            existing.setDateEcheance(tache.getDateEcheance());
            existing.setPriorite(tache.getPriorite());
            existing.setStatut(tache.getStatut());
            existing.setDateModification(LocalDateTime.now());
            
            // Mettre à jour le nom du projet si changé
            if (tache.getProjetId() != null) {
                Optional<FicheProjet> projet = ficheProjetRepository.findById(tache.getProjetId());
                projet.ifPresent(p -> existing.setProjetNom(p.getNom()));
            }
            
            // Vérifier si en retard
            if (existing.getDateEcheance() != null && 
                existing.getDateEcheance().isBefore(LocalDate.now()) &&
                existing.getStatut() != StatutTache.TERMINEE) {
                existing.setStatut(StatutTache.EN_RETARD);
            }
            
            Tache updated = tacheRepository.save(existing);
            
            // TODO: Enregistrer dans l'historique si nécessaire
            
            return updated;
        }
        
        throw new RuntimeException("Tâche non trouvée avec l'ID: " + id);
    }
    
    /**
     * Supprimer une tâche
     */
    public void deleteTache(String id) {
        Optional<Tache> tache = tacheRepository.findById(id);
        
        if (tache.isPresent()) {
            // TODO: Enregistrer dans l'historique si nécessaire
            
            tacheRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tâche non trouvée avec l'ID: " + id);
        }
    }
    
    /**
     * Récupérer les tâches d'un projet
     */
    public List<Tache> getTachesByProjet(String projetId) {
        List<Tache> taches = tacheRepository.findByProjetId(projetId);
        verifierEtMarquerRetards(taches);
        return taches;
    }
    
    /**
     * Calculer les statistiques des tâches d'un utilisateur
     */
    public TacheStats getStatistiques(String userId) {
        List<Tache> taches = getTachesByUtilisateur(userId);
        
        long total = taches.size();
        long aFaire = taches.stream().filter(t -> t.getStatut() == StatutTache.A_FAIRE).count();
        long enCours = taches.stream().filter(t -> t.getStatut() == StatutTache.EN_COURS).count();
        long terminees = taches.stream().filter(t -> t.getStatut() == StatutTache.TERMINEE).count();
        long enRetard = taches.stream().filter(t -> t.getStatut() == StatutTache.EN_RETARD).count();
        
        // Tâches à échéance dans les 7 prochains jours
        LocalDate dans7Jours = LocalDate.now().plusDays(7);
        long prochaines7Jours = taches.stream()
            .filter(t -> t.getDateEcheance() != null)
            .filter(t -> t.getStatut() != StatutTache.TERMINEE)
            .filter(t -> !t.getDateEcheance().isAfter(dans7Jours))
            .filter(t -> !t.getDateEcheance().isBefore(LocalDate.now()))
            .count();
        
        return new TacheStats(total, aFaire, enCours, terminees, enRetard, prochaines7Jours);
    }
    
    /**
     * Vérifier et marquer les tâches en retard
     */
    private void verifierEtMarquerRetards(List<Tache> taches) {
        LocalDate aujourdhui = LocalDate.now();
        
        taches.forEach(tache -> {
            if (tache.getDateEcheance() != null &&
                tache.getDateEcheance().isBefore(aujourdhui) &&
                tache.getStatut() != StatutTache.TERMINEE &&
                tache.getStatut() != StatutTache.EN_RETARD) {
                
                tache.setStatut(StatutTache.EN_RETARD);
                tacheRepository.save(tache);
            }
        });
    }
    
    /**
     * Marquer une tâche comme terminée
     */
    public Tache marquerTerminee(String id, String userId) {
        Optional<Tache> tacheOpt = tacheRepository.findById(id);
        
        if (tacheOpt.isPresent()) {
            Tache tache = tacheOpt.get();
            tache.setStatut(StatutTache.TERMINEE);
            tache.setDateModification(LocalDateTime.now());
            
            Tache updated = tacheRepository.save(tache);
            
            // TODO: Enregistrer dans l'historique si nécessaire
            
            return updated;
        }
        
        throw new RuntimeException("Tâche non trouvée avec l'ID: " + id);
    }
}
