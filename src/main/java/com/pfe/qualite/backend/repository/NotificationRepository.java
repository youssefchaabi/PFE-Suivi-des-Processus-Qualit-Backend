package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUtilisateurId(String utilisateurId);
    List<Notification> findByUtilisateurIdAndLuFalse(String utilisateurId);
}
