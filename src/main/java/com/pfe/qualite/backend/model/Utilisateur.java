package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {
    @Id
    private String id;
    private String nom;
    private String email;
    private String password;
    private String role; // ADMIN, CHEF_PROJET, PILOTE_QUALITE
    private Boolean actif; // true = actif, false = désactivé
    private String telephone;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creePar; // ID de l'utilisateur qui a créé ce compte
    
    // Champs pour la réinitialisation du mot de passe
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;
}
