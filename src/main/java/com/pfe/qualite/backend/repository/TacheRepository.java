package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.Tache;
import com.pfe.qualite.backend.model.StatutTache;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TacheRepository extends MongoRepository<Tache, String> {
    
    List<Tache> findByCreePar(String userId);
    
    List<Tache> findByProjetId(String projetId);
    
    List<Tache> findByStatut(StatutTache statut);
    
    List<Tache> findByCreeParOrderByDateEcheanceAsc(String userId);
    
    List<Tache> findByDateEcheanceBeforeAndStatutNot(LocalDate date, StatutTache statut);
    
    long countByCreePar(String userId);
    
    long countByCreeParAndStatut(String userId, StatutTache statut);
}
