package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.FormulaireObligatoire;
import com.pfe.qualite.backend.repository.FormulaireObligatoireRepository;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FormulaireObligatoireService {

    @Autowired
    private FormulaireObligatoireRepository formulaireRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MailService mailService;

    /**
     * Créer un nouveau formulaire obligatoire
     */
    public FormulaireObligatoire creerFormulaireObligatoire(FormulaireObligatoire formulaire) {
        formulaire.setDateCreation(new Date());
        formulaire.setStatut("EN_ATTENTE");
        formulaire.setNotifie(false);
        formulaire.setNombreNotifications(0);
        
        // Récupérer le nom du responsable
        if (formulaire.getResponsableId() != null) {
            Optional<com.pfe.qualite.backend.model.Utilisateur> utilisateur = 
                utilisateurRepository.findById(formulaire.getResponsableId());
            if (utilisateur.isPresent()) {
                formulaire.setResponsableNom(utilisateur.get().getNom());
            }
        }

        return formulaireRepository.save(formulaire);
    }

    /**
     * Mettre à jour un formulaire obligatoire
     */
    public FormulaireObligatoire updateFormulaireObligatoire(String id, FormulaireObligatoire updated) {
        return formulaireRepository.findById(id).map(formulaire -> {
            formulaire.setNom(updated.getNom());
            formulaire.setDescription(updated.getDescription());
            formulaire.setTypeFormulaire(updated.getTypeFormulaire());
            formulaire.setProjetId(updated.getProjetId());
            formulaire.setResponsableId(updated.getResponsableId());
            formulaire.setResponsableNom(updated.getResponsableNom());
            formulaire.setDateEcheance(updated.getDateEcheance());
            formulaire.setStatut(updated.getStatut());
            formulaire.setPriorite(updated.getPriorite());
            formulaire.setCommentaire(updated.getCommentaire());
            return formulaireRepository.save(formulaire);
        }).orElseThrow(() -> new RuntimeException("Formulaire obligatoire non trouvé"));
    }

    /**
     * Marquer un formulaire comme soumis
     */
    public FormulaireObligatoire marquerCommeSoumis(String id) {
        return formulaireRepository.findById(id).map(formulaire -> {
            formulaire.setStatut("SOUMIS");
            return formulaireRepository.save(formulaire);
        }).orElseThrow(() -> new RuntimeException("Formulaire obligatoire non trouvé"));
    }

    /**
     * Marquer un formulaire comme en retard
     */
    public FormulaireObligatoire marquerCommeEnRetard(String id) {
        return formulaireRepository.findById(id).map(formulaire -> {
            formulaire.setStatut("EN_RETARD");
            return formulaireRepository.save(formulaire);
        }).orElseThrow(() -> new RuntimeException("Formulaire obligatoire non trouvé"));
    }

    /**
     * Récupérer tous les formulaires obligatoires
     */
    public List<FormulaireObligatoire> getAllFormulairesObligatoires() {
        return formulaireRepository.findAll();
    }

    /**
     * Récupérer les formulaires d'un responsable
     */
    public List<FormulaireObligatoire> getFormulairesByResponsable(String responsableId) {
        return formulaireRepository.findByResponsableIdOrderByDateEcheanceAsc(responsableId);
    }

    /**
     * Récupérer les formulaires d'un projet
     */
    public List<FormulaireObligatoire> getFormulairesByProjet(String projetId) {
        return formulaireRepository.findByProjetIdOrderByDateEcheanceAsc(projetId);
    }

    /**
     * Récupérer les formulaires en retard
     */
    public List<FormulaireObligatoire> getFormulairesEnRetard() {
        return formulaireRepository.findByDateEcheanceBeforeAndStatutNotSoumis(new Date());
    }

    /**
     * Récupérer les formulaires à échéance proche (dans les 3 jours)
     */
    public List<FormulaireObligatoire> getFormulairesEcheanceProche() {
        Calendar cal = Calendar.getInstance();
        Date dateDebut = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        Date dateFin = cal.getTime();
        
        return formulaireRepository.findByDateEcheanceBetweenAndStatutEnAttente(dateDebut, dateFin);
    }

    /**
     * Récupérer les formulaires par statut
     */
    public List<FormulaireObligatoire> getFormulairesByStatut(String statut) {
        return formulaireRepository.findByStatutOrderByDateEcheanceAsc(statut);
    }

    /**
     * Récupérer les formulaires par priorité
     */
    public List<FormulaireObligatoire> getFormulairesByPriorite(String priorite) {
        return formulaireRepository.findByPrioriteOrderByDateEcheanceAsc(priorite);
    }

    /**
     * Supprimer un formulaire obligatoire
     */
    public void deleteFormulaireObligatoire(String id) {
        formulaireRepository.deleteById(id);
    }

    /**
     * Vérifier et notifier les retards
     */
    public void verifierEtNotifierRetards() {
        List<FormulaireObligatoire> retards = getFormulairesEnRetard();
        
        for (FormulaireObligatoire retard : retards) {
            if (!retard.isNotifie() || retard.getNombreNotifications() < 3) {
                // Envoyer notification
                notificationService.creerNotification(
                    "Formulaire obligatoire en retard : " + retard.getNom(),
                    retard.getResponsableId(),
                    "FORMULAIRE_RETARD",
                    retard.getId()
                );

                // Envoyer email
                try {
                    Optional<com.pfe.qualite.backend.model.Utilisateur> utilisateur = 
                        utilisateurRepository.findById(retard.getResponsableId());
                    if (utilisateur.isPresent()) {
                        mailService.envoyerEmailRetard(
                            utilisateur.get().getEmail(),
                            retard.getNom(),
                            retard.getDateEcheance()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'envoi d'email: " + e.getMessage());
                }

                // Mettre à jour le statut de notification
                retard.setNotifie(true);
                retard.setDateNotification(new Date());
                retard.setNombreNotifications(retard.getNombreNotifications() + 1);
                formulaireRepository.save(retard);
            }
        }
    }

    /**
     * Vérifier et notifier les échéances proches
     */
    public void verifierEtNotifierEcheancesProches() {
        List<FormulaireObligatoire> echeancesProches = getFormulairesEcheanceProche();
        
        for (FormulaireObligatoire echeance : echeancesProches) {
            if (!echeance.isNotifie()) {
                // Envoyer notification
                notificationService.creerNotification(
                    "Échéance proche pour : " + echeance.getNom(),
                    echeance.getResponsableId(),
                    "FORMULAIRE_ECHEANCE",
                    echeance.getId()
                );

                // Mettre à jour le statut de notification
                echeance.setNotifie(true);
                echeance.setDateNotification(new Date());
                echeance.setNombreNotifications(echeance.getNombreNotifications() + 1);
                formulaireRepository.save(echeance);
            }
        }
    }

    /**
     * Obtenir les statistiques
     */
    public long getNombreFormulairesEnRetard() {
        return formulaireRepository.countByDateEcheanceBeforeAndStatutNotSoumis(new Date());
    }

    public long getNombreFormulairesByStatut(String statut) {
        return formulaireRepository.countByStatut(statut);
    }

    public long getNombreFormulairesByResponsable(String responsableId) {
        return formulaireRepository.countByResponsableId(responsableId);
    }
} 