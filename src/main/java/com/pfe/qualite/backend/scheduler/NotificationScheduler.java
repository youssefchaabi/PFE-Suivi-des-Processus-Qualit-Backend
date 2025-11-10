package com.pfe.qualite.backend.scheduler;

import com.pfe.qualite.backend.model.Notification;
import com.pfe.qualite.backend.model.FormulaireObligatoire;
import com.pfe.qualite.backend.model.FicheQualite;
import com.pfe.qualite.backend.model.FicheSuivi;
import com.pfe.qualite.backend.repository.NotificationRepository;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import com.pfe.qualite.backend.repository.FormulaireObligatoireRepository;
import com.pfe.qualite.backend.repository.FicheQualiteRepository;
import com.pfe.qualite.backend.repository.FicheSuiviRepository;
import com.pfe.qualite.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private FormulaireObligatoireRepository formulaireObligatoireRepository;

    @Autowired
    private FicheQualiteRepository ficheQualiteRepository;

    @Autowired
    private FicheSuiviRepository ficheSuiviRepository;

    @Autowired
    private MailService mailService;

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Scheduled(cron = "0 */15 * * * *") // toutes les 15 minutes
    public void envoyerEmailsNotifications() {
        log.info("⏰ Planificateur exécuté...");

        // ✅ Étape 1 : récupérer les utilisateurs avec des notifications non lues
        List<String> utilisateursAvecNotif = notificationRepository
                .findAll()
                .stream()
                .filter(notif -> !notif.isLu())
                .map(Notification::getUtilisateurId)
                .distinct()
                .collect(Collectors.toList());

        if (utilisateursAvecNotif.isEmpty()) {
            log.info("✅ Aucune notification non lue à envoyer.");
            return;
        }

        // ✅ Étape 2 : REMPLACE ta boucle par ce bloc :
        for (String userId : utilisateursAvecNotif) {
            List<Notification> notifsNonLues = notificationRepository.findByUtilisateurIdAndLuFalse(userId);

            // 🎯 Récupérer l’utilisateur depuis MongoDB
            utilisateurRepository.findById(userId).ifPresentOrElse(utilisateur -> {
                String email = utilisateur.getEmail();  // ✅ E-mail réel

                if (email == null || email.isBlank()) {
                    log.warn("⚠️ Utilisateur {} n’a pas d’e-mail défini. Notification ignorée.", userId);
                    return;
                }

                // 📨 Construire le contenu
                String contenu = notifsNonLues.stream()
                        .map(Notification::getMessage)
                        .collect(Collectors.joining("\n"));

                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(email);
                    message.setSubject("📢 Notifications non lues");
                    message.setText("Bonjour,\n\nVous avez des notifications :\n\n" + contenu);
                    mailSender.send(message);

                    log.info("📧 Email envoyé à {}", email);

                    // ✅ Marquer comme lues
                    notifsNonLues.forEach(n -> {
                        n.setLu(true);
                        notificationRepository.save(n);
                    });

                } catch (Exception e) {
                    log.error("❌ Erreur d'envoi de mail pour utilisateur {}", userId, e);
                }

            }, () -> log.warn("❌ Aucun utilisateur trouvé avec l’ID {}", userId));
        }
    }

    /**
     * Vérifier les fiches de qualité en retard (toutes les 2 minutes)
     */
    @Scheduled(cron = "0 */2 * * * *") // toutes les 2 minutes
    public void verifierFichesQualiteEnRetard() {
        log.info("⏰ Vérification des fiches de qualité en retard...");
        
        List<FicheQualite> toutesLesFiches = ficheQualiteRepository.findAll();
        int compteur = 0;
        
        for (FicheQualite fiche : toutesLesFiches) {
            // Vérifier si la fiche a une date d'échéance et n'est pas terminée
            if (fiche.getDateEcheance() != null && 
                fiche.getResponsable() != null &&
                !"TERMINEE".equals(fiche.getStatut()) && 
                !"VALIDEE".equals(fiche.getStatut()) &&
                !"CLOTUREE".equals(fiche.getStatut())) {
                
                // Vérifier si la date est dépassée
                if (fiche.getDateEcheance().isBefore(java.time.LocalDate.now())) {
                    compteur++;
                    
                    // Chercher une notification existante pour cette fiche
                    List<Notification> toutesNotifs = notificationRepository.findAll();
                    Notification notifExistante = toutesNotifs.stream()
                        .filter(n -> n.getObjetId() != null && 
                                    n.getObjetId().equals(fiche.getId()) &&
                                    "RETARD".equals(n.getType()))
                        .findFirst()
                        .orElse(null);
                    
                    if (notifExistante == null) {
                        // 🆕 Pas de notification existante → Créer et envoyer email
                        String emailResponsable = fiche.getResponsable();
                        
                        utilisateurRepository.findByEmail(emailResponsable).ifPresentOrElse(utilisateur -> {
                            // Créer une notification
                            Notification notification = Notification.builder()
                                .message("⚠️ La fiche qualité '" + fiche.getTitre() + "' est en retard (échéance: " + fiche.getDateEcheance() + ")")
                                .type("RETARD")
                                .lu(false)
                                .dateCreation(new Date())
                                .utilisateurId(utilisateur.getId())
                                .objetId(fiche.getId())
                                .dateDernierEmail(new Date())  // Marquer l'envoi d'email
                                .build();
                            
                            notificationRepository.save(notification);
                            log.info("📢 Notification créée pour la fiche qualité en retard: {}", fiche.getTitre());
                            
                            // 📧 Envoyer le premier email
                            envoyerEmailRetard(emailResponsable, fiche);
                        }, () -> {
                            log.warn("⚠️ Aucun utilisateur trouvé avec l'email: {}", emailResponsable);
                        });
                        
                    } else if (!notifExistante.isLu()) {
                        // 🔄 Notification existante NON LUE → Vérifier si 3 minutes écoulées
                        Date maintenant = new Date();
                        Date dernierEmail = notifExistante.getDateDernierEmail();
                        
                        if (dernierEmail != null) {
                            long minutesEcoulees = (maintenant.getTime() - dernierEmail.getTime()) / (60 * 1000);
                            
                            if (minutesEcoulees >= 3) {
                                // 📧 Plus de 3 minutes → Renvoyer l'email
                                String emailResponsable = fiche.getResponsable();
                                log.info("🔔 Relance après {} minutes pour la fiche: {}", minutesEcoulees, fiche.getTitre());
                                
                                envoyerEmailRetard(emailResponsable, fiche);
                                
                                // Mettre à jour la date du dernier email
                                notifExistante.setDateDernierEmail(new Date());
                                notificationRepository.save(notifExistante);
                            } else {
                                log.info("⏳ Notification non lue mais délai non écoulé ({} min) pour: {}", 
                                        minutesEcoulees, fiche.getTitre());
                            }
                        }
                    } else {
                        // ✅ Notification LUE → Ne rien faire
                        log.info("✅ Notification lue pour la fiche: {}", fiche.getTitre());
                    }
                }
            }
        }
        
        if (compteur > 0) {
            log.info("⚠️ {} fiches de qualité en retard détectées", compteur);
        } else {
            log.info("✅ Aucune fiche de qualité en retard");
        }
    }
    
    /**
     * Méthode utilitaire pour envoyer un email de retard
     */
    private void envoyerEmailRetard(String emailResponsable, FicheQualite fiche) {
        try {
            String sujet = "⚠️ Fiche Qualité en Retard - " + fiche.getTitre();
            String corps = String.format(
                "Bonjour,\n\n" +
                "La fiche qualité '%s' est en retard.\n\n" +
                "Date d'échéance : %s\n" +
                "Statut actuel : %s\n\n" +
                "Veuillez traiter cette fiche dans les plus brefs délais.\n\n" +
                "Cordialement,\n" +
                "Système de Suivi Qualité",
                fiche.getTitre(),
                fiche.getDateEcheance(),
                fiche.getStatut()
            );
            
            mailService.sendEmail(emailResponsable, sujet, corps);
            log.info("📧 Email envoyé à {} pour la fiche {}", emailResponsable, fiche.getTitre());
        } catch (Exception e) {
            log.error("❌ Erreur d'envoi d'email pour {}: {}", emailResponsable, e.getMessage());
        }
    }

    /**
     * Vérifier les formulaires obligatoires en retard (toutes les heures)
     */
    @Scheduled(cron = "0 0 * * * *") // toutes les heures
    public void verifierFormulairesEnRetard() {
        log.info("⏰ Vérification des formulaires en retard...");
        
        Date maintenant = new Date();
        List<FormulaireObligatoire> formulairesEnRetard = formulaireObligatoireRepository
                .findByDateEcheanceBeforeAndStatutNotSoumis(maintenant);
        
        for (FormulaireObligatoire formulaire : formulairesEnRetard) {
            // Marquer comme en retard
            formulaire.setStatut("EN_RETARD");
            formulaireObligatoireRepository.save(formulaire);
            
            // Envoyer email de notification
            utilisateurRepository.findById(formulaire.getResponsableId()).ifPresent(utilisateur -> {
                if (utilisateur.getEmail() != null && !utilisateur.getEmail().isBlank()) {
                    try {
                        mailService.envoyerEmailRetard(
                            utilisateur.getEmail(),
                            formulaire.getNom(),
                            formulaire.getDateEcheance()
                        );
                        log.info("📧 Email de retard envoyé à {} pour le formulaire {}", 
                                utilisateur.getEmail(), formulaire.getNom());
                    } catch (Exception e) {
                        log.error("❌ Erreur d'envoi d'email de retard pour {}", utilisateur.getEmail(), e);
                    }
                }
            });
        }
        
        if (!formulairesEnRetard.isEmpty()) {
            log.info("⚠️ {} formulaires marqués comme en retard", formulairesEnRetard.size());
        }
    }

    /**
     * Vérifier les échéances proches (toutes les 6 heures)
     */
    @Scheduled(cron = "0 0 */6 * * *") // toutes les 6 heures
    public void verifierEcheancesProches() {
        log.info("⏰ Vérification des échéances proches...");
        
        Date maintenant = new Date();
        Date dans24h = new Date(maintenant.getTime() + 24 * 60 * 60 * 1000); // +24h
        
        List<FormulaireObligatoire> formulairesEcheanceProche = formulaireObligatoireRepository
                .findByDateEcheanceBetweenAndStatutEnAttente(maintenant, dans24h);
        
        for (FormulaireObligatoire formulaire : formulairesEcheanceProche) {
            utilisateurRepository.findById(formulaire.getResponsableId()).ifPresent(utilisateur -> {
                if (utilisateur.getEmail() != null && !utilisateur.getEmail().isBlank()) {
                    try {
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setTo(utilisateur.getEmail());
                        message.setSubject("⚠️ Échéance proche - " + formulaire.getNom());
                        message.setText(String.format(
                            "Bonjour,\n\n" +
                            "Le formulaire obligatoire '%s' arrive à échéance le %s.\n" +
                            "Veuillez le compléter dans les plus brefs délais.\n\n" +
                            "Cordialement,\n" +
                            "Système de Suivi Qualité",
                            formulaire.getNom(),
                            formulaire.getDateEcheance().toString()
                        ));
                        mailSender.send(message);
                        
                        log.info("📧 Email d'échéance proche envoyé à {} pour le formulaire {}", 
                                utilisateur.getEmail(), formulaire.getNom());
                    } catch (Exception e) {
                        log.error("❌ Erreur d'envoi d'email d'échéance pour {}", utilisateur.getEmail(), e);
                    }
                }
            });
        }
        
        if (!formulairesEcheanceProche.isEmpty()) {
            log.info("⚠️ {} formulaires avec échéance proche notifiés", formulairesEcheanceProche.size());
        }
    }
}
