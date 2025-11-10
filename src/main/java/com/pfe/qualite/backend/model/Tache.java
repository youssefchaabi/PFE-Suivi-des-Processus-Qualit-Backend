package com.pfe.qualite.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "taches")
public class Tache {
    
    @Id
    private String id;
    
    private String titre;
    private String description;
    
    @Indexed
    private String projetId;
    private String projetNom;
    
    @Indexed
    private LocalDate dateEcheance;
    
    private PrioriteTache priorite;
    
    @Indexed
    private StatutTache statut;
    
    @Indexed
    private String creePar;
    
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    // Constructeurs
    public Tache() {
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutTache.A_FAIRE;
        this.priorite = PrioriteTache.MOYENNE;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public void setTitre(String titre) {
        this.titre = titre;
    }
    
    public String getDescription() {
        return description;
    }

    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProjetId() {
        return projetId;
    }
    
    public void setProjetId(String projetId) {
        this.projetId = projetId;
    }
    
    public String getProjetNom() {
        return projetNom;
    }
    
    public void setProjetNom(String projetNom) {
        this.projetNom = projetNom;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public PrioriteTache getPriorite() {
        return priorite;
    }
    
    public void setPriorite(PrioriteTache priorite) {
        this.priorite = priorite;
    }
    
    public StatutTache getStatut() {
        return statut;
    }
    
    public void setStatut(StatutTache statut) {
        this.statut = statut;
    }
    
    public String getCreePar() {
        return creePar;
    }
    
    public void setCreePar(String creePar) {
        this.creePar = creePar;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }
}
