package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.FormulaireObligatoire;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FormulaireObligatoireRepository extends MongoRepository<FormulaireObligatoire, String> {

    // Trouver par responsable
    List<FormulaireObligatoire> findByResponsableIdOrderByDateEcheanceAsc(String responsableId);

    // Trouver par projet
    List<FormulaireObligatoire> findByProjetIdOrderByDateEcheanceAsc(String projetId);

    // Trouver par statut
    List<FormulaireObligatoire> findByStatutOrderByDateEcheanceAsc(String statut);

    // Trouver par type de formulaire
    List<FormulaireObligatoire> findByTypeFormulaireOrderByDateEcheanceAsc(String typeFormulaire);

    // Trouver les formulaires en retard
    @Query("{'dateEcheance': {$lt: ?0}, 'statut': {$ne: 'SOUMIS'}}")
    List<FormulaireObligatoire> findByDateEcheanceBeforeAndStatutNotSoumis(Date date);

    // Trouver les formulaires à échéance proche (dans les 3 jours)
    @Query("{'dateEcheance': {$gte: ?0, $lte: ?1}, 'statut': 'EN_ATTENTE'}")
    List<FormulaireObligatoire> findByDateEcheanceBetweenAndStatutEnAttente(Date dateDebut, Date dateFin);

    // Trouver par priorité
    List<FormulaireObligatoire> findByPrioriteOrderByDateEcheanceAsc(String priorite);

    // Trouver les formulaires non notifiés
    List<FormulaireObligatoire> findByNotifieFalse();

    // Compter par statut
    long countByStatut(String statut);

    // Compter par responsable
    long countByResponsableId(String responsableId);

    // Compter les retards
    @Query("{'dateEcheance': {$lt: ?0}, 'statut': {$ne: 'SOUMIS'}}")
    long countByDateEcheanceBeforeAndStatutNotSoumis(Date date);
} 