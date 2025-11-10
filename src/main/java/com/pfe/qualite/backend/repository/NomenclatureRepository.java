package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.Nomenclature;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NomenclatureRepository extends MongoRepository<Nomenclature, String> {
    List<Nomenclature> findByType(String type);
}
