package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Document(collection = "fiches_qualite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheQualite {

    @Id
    private String id;

    private String titre;
    private String description;
    private String typeFiche;        // Code de la nomenclature TYPE_FICHE
    private String statut;           // Code de la nomenclature STATUT
    private String categorie;        // Code de la nomenclature CATEGORIE_PROJET (optionnel)
    private String priorite;         // Code de la nomenclature PRIORITE (optionnel)
    private String responsable;      // Email de l'utilisateur responsable
    private LocalDate dateEcheance;  // Date d'échéance
    private String observations;     // Observations complémentaires
    
    // Métadonnées
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String creePar;          // Email de l'utilisateur créateur
    private String modifiePar;       // Email du dernier modificateur
}
