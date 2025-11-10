package com.pfe.qualite.backend.model;

public class TacheStats {
    
    private long total;
    private long aFaire;
    private long enCours;
    private long terminees;
    private long enRetard;
    private long prochaines7Jours;
    
    // Constructeur
    public TacheStats() {
    }
    
    public TacheStats(long total, long aFaire, long enCours, long terminees, long enRetard, long prochaines7Jours) {
        this.total = total;
        this.aFaire = aFaire;
        this.enCours = enCours;
        this.terminees = terminees;
        this.enRetard = enRetard;
        this.prochaines7Jours = prochaines7Jours;
    }
    
    // Getters et Setters
    public long getTotal() {
        return total;
    }
    
    public void setTotal(long total) {
        this.total = total;
    }
    
    public long getaFaire() {
        return aFaire;
    }
    
    public void setaFaire(long aFaire) {
        this.aFaire = aFaire;
    }
    
    public long getEnCours() {
        return enCours;
    }
    
    public void setEnCours(long enCours) {
        this.enCours = enCours;
    }
    
    public long getTerminees() {
        return terminees;
    }
    
    public void setTerminees(long terminees) {
        this.terminees = terminees;
    }
    
    public long getEnRetard() {
        return enRetard;
    }
    
    public void setEnRetard(long enRetard) {
        this.enRetard = enRetard;
    }
    
    public long getProchaines7Jours() {
        return prochaines7Jours;
    }
    
    public void setProchaines7Jours(long prochaines7Jours) {
        this.prochaines7Jours = prochaines7Jours;
    }
}
