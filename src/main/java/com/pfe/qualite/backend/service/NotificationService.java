package com.pfe.qualite.backend.service;

import com.pfe.qualite.backend.model.Notification;
import com.pfe.qualite.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Créer une notification dans la base de données
     */
    public void creerNotification(String message, String utilisateurId, String type, String objetId) {
        // Vérification : l'utilisateurId doit être un ID MongoDB valide (24 caractères hexadécimaux)
        if (utilisateurId == null || !utilisateurId.matches("^[a-fA-F0-9]{24}$")) {
            logger.error("[NotificationService] utilisateurId invalide : '{}' (notification ignorée)", utilisateurId);
            return;
        }
        Notification notification = Notification.builder()
                .message(message)
                .utilisateurId(utilisateurId)
                .type(type)
                .objetId(objetId)
                .lu(false)
                .dateCreation(new java.util.Date())
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Envoyer une notification (base de données + optionnel email)
     */
    public void envoyerNotification(String utilisateurId, String titre, String message, String type) {
        logger.info("Envoi notification à l'utilisateur: {}", utilisateurId);
        creerNotification(message, utilisateurId, type, null);
    }

    /**
     * Envoyer un email de bienvenue avec mot de passe temporaire
     */
    public void envoyerEmailBienvenue(String email, String nom, String motDePasseTemporaire) {
        logger.info("Envoi email de bienvenue à: {}", email);
        
        try {
            String sujet = "Bienvenue sur l'application Qualité Pro";
            String corps = String.format(
                "Bonjour %s,\n\n" +
                "Votre compte a été créé avec succès sur l'application Qualité Pro.\n\n" +
                "Vos identifiants de connexion :\n" +
                "Email : %s\n" +
                "Mot de passe temporaire : %s\n\n" +
                "Pour des raisons de sécurité, nous vous recommandons de changer votre mot de passe lors de votre première connexion.\n\n" +
                "Cordialement,\n" +
                "L'équipe Qualité Pro",
                nom, email, motDePasseTemporaire
            );
            
            emailService.envoyerEmail(email, sujet, corps);
            logger.info("Email de bienvenue envoyé avec succès à: {}", email);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue", e);
            // Ne pas bloquer la création du compte si l'email échoue
        }
    }

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    public void envoyerEmailResetPassword(String email, String nom, String nouveauMotDePasse) {
        logger.info("Envoi email de réinitialisation de mot de passe à: {}", email);
        
        try {
            String sujet = "Réinitialisation de votre mot de passe";
            String corps = String.format(
                "Bonjour %s,\n\n" +
                "Votre mot de passe a été réinitialisé par l'administrateur.\n\n" +
                "Votre nouveau mot de passe temporaire : %s\n\n" +
                "Pour des raisons de sécurité, nous vous recommandons de changer ce mot de passe dès votre prochaine connexion.\n\n" +
                "Cordialement,\n" +
                "L'équipe Qualité Pro",
                nom, nouveauMotDePasse
            );
            
            emailService.envoyerEmail(email, sujet, corps);
            logger.info("Email de réinitialisation envoyé avec succès à: {}", email);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de réinitialisation", e);
            // Ne pas bloquer la réinitialisation si l'email échoue
        }
    }
}
