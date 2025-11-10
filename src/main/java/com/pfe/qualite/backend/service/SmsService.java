package com.pfe.qualite.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${notifications.sms.enabled:false}")
    private boolean smsEnabled;

    // Stub d'envoi SMS prêt à brancher sur un fournisseur (Twilio, etc.)
    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.debug("SMS désactivé. Message non envoyé à {}", phoneNumber);
            return;
        }
        // Intégration fournisseur à implémenter ici
        log.info("[SMS-STUB] Envoi SMS à {}: {}", phoneNumber, message);
    }
}





