package com.pfe.qualite.backend.security.auth;

import com.pfe.qualite.backend.model.Utilisateur;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import com.pfe.qualite.backend.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtUtils jwtUtils;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public String login(LoginRequest request) {
        log.info("Tentative de login pour email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Échec d'authentification pour {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        log.info("Utilisateur trouvé: {} | Rôle: {}", utilisateur.getEmail(), utilisateur.getRole());
        return jwtUtils.generateToken(utilisateur.getId(), utilisateur.getEmail(), utilisateur.getRole());
    }

    public void forgotPassword(String email) {
        log.info("Demande de réinitialisation de mot de passe pour: {}", email);
        
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte associé à cet e-mail"));

        // Générer un token unique
        String token = UUID.randomUUID().toString();
        utilisateur.setResetPasswordToken(token);
        utilisateur.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1)); // Valide 1 heure
        utilisateurRepository.save(utilisateur);

        // Envoyer l'e-mail
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        sendResetPasswordEmail(utilisateur.getEmail(), utilisateur.getNom(), resetLink);
        
        log.info("E-mail de réinitialisation envoyé à: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        log.info("Tentative de réinitialisation de mot de passe avec token");
        
        Utilisateur utilisateur = utilisateurRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        // Vérifier l'expiration du token
        if (utilisateur.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Le lien de réinitialisation a expiré. Veuillez faire une nouvelle demande.");
        }

        // Mettre à jour le mot de passe
        utilisateur.setPassword(passwordEncoder.encode(newPassword));
        utilisateur.setResetPasswordToken(null);
        utilisateur.setResetPasswordTokenExpiry(null);
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        log.info("Mot de passe réinitialisé avec succès pour: {}", utilisateur.getEmail());
    }

    private void sendResetPasswordEmail(String toEmail, String userName, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Réinitialisation de votre mot de passe - Suivi Qualité");
            message.setText(
                "Bonjour " + userName + ",\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien ci-dessous pour créer un nouveau mot de passe :\n" +
                resetLink + "\n\n" +
                "Ce lien est valide pendant 1 heure.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet e-mail.\n\n" +
                "Cordialement,\n" +
                "L'équipe Suivi Qualité"
            );
            mailSender.send(message);
            log.info("E-mail de réinitialisation envoyé avec succès à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'e-mail à {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail");
        }
    }
}
