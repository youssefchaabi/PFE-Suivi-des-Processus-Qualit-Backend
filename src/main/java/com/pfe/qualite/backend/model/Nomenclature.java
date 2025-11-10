package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "nomenclatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nomenclature {
    @Id
    private String id;
    private String type;     // TYPE_FICHE, STATUT, CATEGORIE_PROJET
    private String code;     // Code unique (ex: FICHE_PROJET, EN_COURS)
    private String libelle;  // Libellé affiché (ex: "Fiche Projet", "En cours")
    private String description;
    private Boolean actif;   // true = actif, false = désactivé
    private Integer ordre;   // Ordre d'affichage
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
