package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "historique_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueAction {

    @Id
    private String id;

    private String action; // CREATE, UPDATE, DELETE, LOGIN, NOTIFICATION
    private String entite; // FICHE_QUALITE, FICHE_SUIVI, FICHE_PROJET, UTILISATEUR, NOMENCLATURE
    private String entiteId; // ID de l'entité concernée
    private String utilisateurId; // ID de l'utilisateur qui a effectué l'action
    private String utilisateurNom; // Nom de l'utilisateur
    private String details; // Détails de l'action
    private Date dateAction;
    private String anciennesValeurs; // JSON des anciennes valeurs (pour UPDATE)
    private String nouvellesValeurs; // JSON des nouvelles valeurs (pour UPDATE)
    private String ipAdresse; // Adresse IP de l'utilisateur
    private String userAgent; // Navigateur/application utilisée
} 