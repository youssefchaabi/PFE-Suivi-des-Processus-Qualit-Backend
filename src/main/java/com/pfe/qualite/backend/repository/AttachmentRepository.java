package com.pfe.qualite.backend.repository;

import com.pfe.qualite.backend.model.Attachment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AttachmentRepository extends MongoRepository<Attachment, String> {
    
    /**
     * Trouve toutes les pièces jointes d'une entité
     */
    List<Attachment> findByEntityTypeAndEntityId(String entityType, String entityId);
    
    /**
     * Trouve toutes les pièces jointes uploadées par un utilisateur
     */
    List<Attachment> findByUploadedBy(String uploadedBy);
    
    /**
     * Supprime toutes les pièces jointes d'une entité
     */
    void deleteByEntityTypeAndEntityId(String entityType, String entityId);
}
