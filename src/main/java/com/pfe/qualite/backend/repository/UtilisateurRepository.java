package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.Utilisateur;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UtilisateurRepository extends MongoRepository<Utilisateur, String> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByResetPasswordToken(String token);
}
