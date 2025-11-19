package com.pfe.qualite.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    @ConditionalOnProperty(name = "spring.mail.enabled", havingValue = "true", matchIfMissing = true)
    public JavaMailSender javaMailSender() {
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port);
            
            if (username != null && !username.isEmpty()) {
                mailSender.setUsername(username);
                mailSender.setPassword(password);
            }

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
            props.put("mail.debug", "false");

            System.out.println("✅ JavaMailSender configuré avec succès");
            return mailSender;
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la configuration de JavaMailSender: " + e.getMessage());
            return null;
        }
    }
}
