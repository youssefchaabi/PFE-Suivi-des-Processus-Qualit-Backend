package com.pfe.qualite.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "fiches_projet")
public class FicheProjet {
    @Id
    private String id;
    private String nom;
    private String description;
    private String objectifs;
    private String responsable;
    private Date echeance;
    private String statut;

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getObjectifs() { return objectifs; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public Date getEcheance() { return echeance; }
    public void setEcheance(Date echeance) { this.echeance = echeance; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
} 