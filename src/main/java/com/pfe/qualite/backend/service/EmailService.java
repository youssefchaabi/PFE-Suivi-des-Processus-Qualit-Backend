package com.pfe.qualite.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails avec templates HTML
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envoie un email simple (alias pour compatibilité)
     */
    public void envoyerEmail(String to, String subject, String text) {
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Envoie un email simple
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.setFrom("noreply@qualite-app.com");
            
            mailSender.send(message);
            log.info("Email simple envoyé à: {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur d'envoi d'email", e);
        }
    }

    /**
     * Envoie un email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@qualite-app.com");
            
            mailSender.send(message);
            log.info("Email HTML envoyé à: {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur d'envoi d'email HTML", e);
        }
    }

    /**
     * Envoie un email avec pièce jointe
     */
    public void sendEmailWithAttachment(String to, String subject, String htmlContent, 
                                       byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("noreply@qualite-app.com");
            
            // Ajouter la pièce jointe
            helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            
            mailSender.send(message);
            log.info("Email avec pièce jointe envoyé à: {}", to);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email avec pièce jointe à {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur d'envoi d'email avec pièce jointe", e);
        }
    }

    /**
     * Envoie une notification de nouvelle fiche qualité
     */
    public void sendNewFicheQualiteNotification(String to, String userName, String ficheTitle, String ficheId) {
        String subject = "Nouvelle Fiche Qualité Assignée";
        String htmlContent = buildNewFicheQualiteTemplate(userName, ficheTitle, ficheId);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Envoie une relance pour une fiche en retard
     */
    public void sendFicheRelanceNotification(String to, String userName, String ficheTitle, String ficheId, int joursRetard) {
        String subject = "⚠️ Relance: Fiche Qualité en Retard";
        String htmlContent = buildRelanceTemplate(userName, ficheTitle, ficheId, joursRetard);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Envoie un rapport KPI par email
     */
    public void sendRapportKpi(String to, String userName, byte[] rapportPdf) {
        String subject = "📊 Rapport KPI Qualité";
        String htmlContent = buildRapportKpiTemplate(userName);
        sendEmailWithAttachment(to, subject, htmlContent, rapportPdf, "rapport-kpi.pdf");
    }

    /**
     * Envoie une notification de validation de fiche
     */
    public void sendFicheValidationNotification(String to, String userName, String ficheTitle, boolean isApproved) {
        String subject = isApproved ? "✅ Fiche Qualité Validée" : "❌ Fiche Qualité Rejetée";
        String htmlContent = buildValidationTemplate(userName, ficheTitle, isApproved);
        sendHtmlEmail(to, subject, htmlContent);
    }

    // ==================== TEMPLATES HTML ====================

    /**
     * Template pour nouvelle fiche qualité
     */
    private String buildNewFicheQualiteTemplate(String userName, String ficheTitle, String ficheId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: #667eea; 
                             color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📋 Nouvelle Fiche Qualité</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Une nouvelle fiche qualité vous a été assignée :</p>
                        <div style="background: white; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0;">
                            <h3 style="margin: 0 0 10px 0; color: #667eea;">%s</h3>
                            <p style="margin: 0; color: #666;">ID: %s</p>
                        </div>
                        <p>Veuillez consulter cette fiche et prendre les actions nécessaires.</p>
                        <a href="http://localhost:4200/fiches/%s" class="button">Voir la Fiche</a>
                    </div>
                    <div class="footer">
                        <p>Application de Suivi des Processus Qualité</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, ficheTitle, ficheId, ficheId);
    }

    /**
     * Template pour relance
     */
    private String buildRelanceTemplate(String userName, String ficheTitle, String ficheId, int joursRetard) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .alert-box { background: #fff3cd; border-left: 4px solid #f5576c; padding: 15px; margin: 20px 0; }
                    .button { display: inline-block; padding: 12px 30px; background: #f5576c; 
                             color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Relance Importante</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Nous vous rappelons qu'une fiche qualité nécessite votre attention :</p>
                        <div class="alert-box">
                            <h3 style="margin: 0 0 10px 0; color: #f5576c;">%s</h3>
                            <p style="margin: 0; color: #666;">ID: %s</p>
                            <p style="margin: 10px 0 0 0; color: #d9534f; font-weight: bold;">
                                ⏰ En retard de %d jour(s)
                            </p>
                        </div>
                        <p>Merci de traiter cette fiche dans les plus brefs délais.</p>
                        <a href="http://localhost:4200/fiches/%s" class="button">Traiter Maintenant</a>
                    </div>
                    <div class="footer">
                        <p>Application de Suivi des Processus Qualité</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, ficheTitle, ficheId, joursRetard, ficheId);
    }

    /**
     * Template pour rapport KPI
     */
    private String buildRapportKpiTemplate(String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #4facfe 0%%, #00f2fe 100%%); 
                             color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; border-left: 4px solid #4facfe; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📊 Rapport KPI Qualité</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Veuillez trouver ci-joint votre rapport KPI qualité.</p>
                        <div class="info-box">
                            <h3 style="margin: 0 0 10px 0; color: #4facfe;">📎 Pièce Jointe</h3>
                            <p style="margin: 0; color: #666;">rapport-kpi.pdf</p>
                        </div>
                        <p>Ce rapport contient les statistiques et métriques de performance de vos processus qualité.</p>
                    </div>
                    <div class="footer">
                        <p>Application de Suivi des Processus Qualité</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName);
    }

    /**
     * Template pour validation/rejet
     */
    private String buildValidationTemplate(String userName, String ficheTitle, boolean isApproved) {
        String color = isApproved ? "#10b981" : "#ef4444";
        String status = isApproved ? "Validée" : "Rejetée";
        String icon = isApproved ? "✅" : "❌";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: %s; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .status-box { background: white; padding: 20px; border-left: 4px solid %s; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s Fiche %s</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre fiche qualité a été %s :</p>
                        <div class="status-box">
                            <h3 style="margin: 0 0 10px 0; color: %s;">%s</h3>
                        </div>
                        <p>%s</p>
                    </div>
                    <div class="footer">
                        <p>Application de Suivi des Processus Qualité</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, color, color, icon, status, userName, status.toLowerCase(), 
                 color, ficheTitle, 
                 isApproved ? "Félicitations ! Vous pouvez continuer le processus." 
                            : "Veuillez apporter les corrections nécessaires.");
    }
}
