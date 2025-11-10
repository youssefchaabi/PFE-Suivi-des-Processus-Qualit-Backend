package com.pfe.qualite.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "fiches_suivi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheSuivi {

    @Id
    private String id;

    private String ficheId;
    private Date dateSuivi;
    private String etatAvancement;
    private String problemes;
    private String decisions;
    private String indicateursKpi;
    private Double tauxConformite;
    private Double delaiTraitementJours;
    private String ajoutePar;
}
