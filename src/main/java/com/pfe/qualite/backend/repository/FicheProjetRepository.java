package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.FicheProjet;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FicheProjetRepository extends MongoRepository<FicheProjet, String> {
    List<FicheProjet> findByResponsable(String responsable);
} 