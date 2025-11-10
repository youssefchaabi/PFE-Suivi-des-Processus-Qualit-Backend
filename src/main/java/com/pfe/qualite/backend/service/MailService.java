package com.pfe.qualite.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    @Value("${app.mail.from:}")
    private String defaultFrom;

    @Async
    public void sendEmail(String to, String subject, String body) {
        System.out.println("📨 Envoi de l'e-mail à " + to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (defaultFrom != null && !defaultFrom.isBlank()) {
            message.setFrom(defaultFrom);
        }
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void envoyerEmailRetard(String to, String nomFormulaire, Date dateEcheance) {
        String subject = "⚠️ Formulaire obligatoire en retard";
        String body = String.format(
            "Bonjour,\n\n" +
            "Le formulaire obligatoire '%s' est en retard.\n" +
            "Date d'échéance : %s\n\n" +
            "Veuillez le compléter dans les plus brefs délais.\n\n" +
            "Cordialement,\n" +
            "Système de Suivi Qualité",
            nomFormulaire,
            dateEcheance.toString()
        );
        sendEmail(to, subject, body);
    }

}
