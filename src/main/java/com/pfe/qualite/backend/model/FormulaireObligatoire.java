package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "formulaires_obligatoires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaireObligatoire {

    @Id
    private String id;

    private String nom; // Nom du formulaire obligatoire
    private String description; // Description du formulaire
    private String typeFormulaire; // FICHE_QUALITE, FICHE_SUIVI, FICHE_PROJET
    private String projetId; // ID du projet concerné
    private String responsableId; // ID du responsable (chef de projet)
    private String responsableNom; // Nom du responsable
    private Date dateEcheance; // Date limite de soumission
    private Date dateCreation; // Date de création du formulaire obligatoire
    private String statut; // EN_ATTENTE, SOUMIS, EN_RETARD, ANNULE
    private String priorite; // HAUTE, MOYENNE, BASSE
    private String commentaire; // Commentaire additionnel
    private boolean notifie; // Si une notification a déjà été envoyée
    private Date dateNotification; // Date de la dernière notification
    private int nombreNotifications; // Nombre de notifications envoyées
} 