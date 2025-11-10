package com.pfe.qualite.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Modèle pour les pièces jointes
 */
@Document(collection = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    
    @Id
    private String id;
    
    /**
     * Nom original du fichier
     */
    private String originalFileName;
    
    /**
     * Nom du fichier stocké (unique)
     */
    private String storedFileName;
    
    /**
     * Type MIME du fichier
     */
    private String contentType;
    
    /**
     * Taille du fichier en bytes
     */
    private Long fileSize;
    
    /**
     * Type d'entité associée (FICHE_QUALITE, FICHE_SUIVI, PROJET, etc.)
     */
    private String entityType;
    
    /**
     * ID de l'entité associée
     */
    private String entityId;
    
    /**
     * Utilisateur qui a uploadé le fichier
     */
    private String uploadedBy;
    
    /**
     * Date d'upload
     */
    private Date uploadedAt;
    
    /**
     * Description optionnelle
     */
    private String description;
}
