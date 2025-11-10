package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.FicheSuivi;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FicheSuiviRepository extends MongoRepository<FicheSuivi, String> {

    // Toutes les fiches de suivi d'une même fiche projet
    List<FicheSuivi> findByFicheId(String ficheId);
    List<FicheSuivi> findByAjoutePar(String ajoutePar);
}
