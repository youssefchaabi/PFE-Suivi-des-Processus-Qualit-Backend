package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.HistoriqueAction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HistoriqueActionRepository extends MongoRepository<HistoriqueAction, String> {

    // Trouver par utilisateur
    List<HistoriqueAction> findByUtilisateurIdOrderByDateActionDesc(String utilisateurId);

    // Trouver par entité
    List<HistoriqueAction> findByEntiteAndEntiteIdOrderByDateActionDesc(String entite, String entiteId);

    // Trouver par type d'action
    List<HistoriqueAction> findByActionOrderByDateActionDesc(String action);

    // Trouver par période
    @Query("{'dateAction': {$gte: ?0, $lte: ?1}}")
    List<HistoriqueAction> findByDateActionBetweenOrderByDateActionDesc(Date dateDebut, Date dateFin);

    // Trouver par utilisateur et période
    @Query("{'utilisateurId': ?0, 'dateAction': {$gte: ?1, $lte: ?2}}")
    List<HistoriqueAction> findByUtilisateurIdAndDateActionBetweenOrderByDateActionDesc(String utilisateurId, Date dateDebut, Date dateFin);

    // Trouver par entité et période
    @Query("{'entite': ?0, 'dateAction': {$gte: ?1, $lte: ?2}}")
    List<HistoriqueAction> findByEntiteAndDateActionBetweenOrderByDateActionDesc(String entite, Date dateDebut, Date dateFin);

    // Compter les actions par utilisateur
    long countByUtilisateurId(String utilisateurId);

    // Compter les actions par entité
    long countByEntite(String entite);
} 