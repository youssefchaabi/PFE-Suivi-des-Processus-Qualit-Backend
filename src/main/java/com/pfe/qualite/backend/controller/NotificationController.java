package com.pfe.qualite.backend.controller;

import com.pfe.qualite.backend.model.Notification;
import com.pfe.qualite.backend.repository.NotificationRepository;
import com.pfe.qualite.backend.service.MailService;
import com.pfe.qualite.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private com.pfe.qualite.backend.scheduler.NotificationScheduler notificationScheduler;

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        notification.setDateCreation(new Date()); // date de création auto
        return notificationRepository.save(notification);
    }

    // 🔹 GET : toutes les notifications
    @GetMapping
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<Notification> getByUtilisateur(@PathVariable String utilisateurId) {
        return notificationRepository.findByUtilisateurId(utilisateurId);
    }

    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public List<Notification> getNonLues(@PathVariable String utilisateurId) {
        return notificationRepository.findByUtilisateurIdAndLuFalse(utilisateurId);
    }

    @PutMapping("/{id}/lire")
    public Notification marquerCommeLue(@PathVariable String id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            Notification n = notification.get();
            n.setLu(true);
            return notificationRepository.save(n);
        } else {
            throw new RuntimeException("Notification introuvable avec ID : " + id);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable String id) {
        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        notificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/relancer")
    public ResponseEntity<String> relancer(@RequestBody RelanceRequest req) {
        System.out.println("📧 Tentative de relance pour utilisateur: " + req.utilisateurId);
        
        // Vérifier que l'utilisateurId est fourni
        if (req.utilisateurId == null || req.utilisateurId.isBlank()) {
            System.out.println("❌ Aucun utilisateurId fourni");
            return ResponseEntity.badRequest().body("Aucun utilisateur spécifié");
        }
        
        // Récupérer l'utilisateur
        var userOpt = utilisateurRepository.findById(req.utilisateurId);
        if (userOpt.isEmpty()) {
            System.out.println("❌ Utilisateur introuvable: " + req.utilisateurId);
            return ResponseEntity.badRequest().body("Utilisateur introuvable");
        }
        
        var user = userOpt.get();
        System.out.println("✅ Utilisateur trouvé: " + user.getEmail());
        
        // Vérifier que l'utilisateur a un email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            System.out.println("❌ Utilisateur sans email");
            return ResponseEntity.badRequest().body("Utilisateur sans email configuré");
        }
        
        String email = user.getEmail();
        String sujet = "🔔 Relance - Notification en attente";
        String message = req.message != null && !req.message.isBlank() 
            ? req.message 
            : "Bonjour,\n\nVous avez une notification en attente qui nécessite votre attention.\n\nCordialement,\nSystème de Suivi Qualité";
        
        try {
            System.out.println("📨 Envoi de l'email à: " + email);
            mailService.sendEmail(email, sujet, message);
            System.out.println("✅ Email envoyé avec succès");
            return ResponseEntity.ok("Email de relance envoyé avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur d'envoi d'email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur d'envoi: " + e.getMessage());
        }
    }

    /**
     * 🧪 Endpoint de test pour déclencher manuellement la vérification des retards
     */
    @PostMapping("/verifier-retards")
    public ResponseEntity<String> verifierRetards() {
        try {
            notificationScheduler.verifierFichesQualiteEnRetard();
            return ResponseEntity.ok("✅ Vérification des retards effectuée avec succès");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * 🧪 Endpoint de test pour envoyer un email de test
     */
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        try {
            System.out.println("📧 Test d'envoi d'email à: " + email);
            mailService.sendEmail(email, "Test Email", "Ceci est un email de test du système de notifications.");
            return ResponseEntity.ok("✅ Email de test envoyé à " + email);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * 🧪 Endpoint pour vérifier les infos d'un utilisateur
     */
    @GetMapping("/debug/utilisateur/{utilisateurId}")
    public ResponseEntity<?> debugUtilisateur(@PathVariable String utilisateurId) {
        var userOpt = utilisateurRepository.findById(utilisateurId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();
        return ResponseEntity.ok(java.util.Map.of(
            "id", user.getId(),
            "email", user.getEmail() != null ? user.getEmail() : "NON DÉFINI",
            "nom", user.getNom() != null ? user.getNom() : "NON DÉFINI"
        ));
    }

    public static class RelanceRequest {
        public String utilisateurId;
        public String notificationId;
        public String type;
        public String message;
    }
}
